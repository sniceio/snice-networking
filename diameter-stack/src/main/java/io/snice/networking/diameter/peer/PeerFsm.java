package io.snice.networking.diameter.peer;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;
import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.DiameterRequest;
import io.snice.networking.codec.diameter.avp.api.ResultCode;
import io.snice.networking.common.event.ConnectionActiveIOEvent;
import io.snice.networking.common.event.ConnectionAttemptCompletedIOEvent;
import io.snice.networking.common.event.ConnectionConnectAttemptIOEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.snice.networking.diameter.peer.PeerState.CLOSED;
import static io.snice.networking.diameter.peer.PeerState.OPEN;
import static io.snice.networking.diameter.peer.PeerState.TERMINATED;
import static io.snice.networking.diameter.peer.PeerState.WAIT_CEA;
import static io.snice.networking.diameter.peer.PeerState.WAIT_CER;
import static io.snice.networking.diameter.peer.PeerState.WAIT_CONNECT_ACK;

public class PeerFsm {

    private static final Logger logger = LoggerFactory.getLogger(PeerFsm.class);

    public static final Definition<PeerState, PeerContext, PeerData> definition;

    static {
        final var builder = FSM.of(PeerState.class).ofContextType(PeerContext.class).withDataType(PeerData.class);
        final var closed = builder.withInitialState(CLOSED);
        final var open = builder.withState(OPEN);
        final var waitCer = builder.withState(WAIT_CER);
        final var waitCea = builder.withState(WAIT_CEA);
        final var waitConnAck = builder.withState(WAIT_CONNECT_ACK);
        final var terminated = builder.withFinalState(PeerState.TERMINATED);


        /**
         * These are the transitions as outlined in RFC6733 for the Peer state
         * machine. Despite events and actions being labeled 'R' (receiving)
         * and 'I' (initiating) they are all the same and as such, they are
         * implemented as a single state machine instead of one for "R" and one
         * for "I" side.
         *
         * In addition for the transitions below, Hektor.io requires a final
         * state (which the Peer FSM doesn't really define per se) so there
         * is a final TERMINATED state as part of this implementation.
         *
         * Also, the event "Conn-Active" and the state "Wait-CER" isn't part
         * of the RFC but this will improve the resiliency of the FSM
         * and the stack in general. See comments on the state and their
         * transitions.
         *
         * And per RFC, there is a "start" event but the wait that Snice Networking
         * and the Peer FSM is setup, that start event is driven by the fact that
         * we create an underlying channel, which tne will get a "conn-active" event
         * if all goes well.
         *
         * state      event                 action            next state
         * ---------------------------------------------------------------
         * Closed    Conn-Active(inbound)   Start-CER-Timer   Wait-CER
         *           Conn-Active(outbound)  Send-CER          Wait-CEA
         */

        /**
         * The "start" event in the context of Snice Networking is an attempt to make
         * and outbound connection, which is signaled by the {@link ConnectionConnectAttemptIOEvent} event
         * and an attempt to connect has already been made which is why it will transition to Wait-Conn-Ack
         * right away.
         *
         * While in this state, Snice Networking will either issue a {@link ConnectionActiveIOEvent}, if
         * a successful attempt has been made, or a XXX event if things failed.
         */
        closed.transitionTo(WAIT_CEA).onEvent(ConnectionActiveIOEvent.class).withGuard(ConnectionActiveIOEvent::isOutboundConnection).withAction(PeerFsm::sendCer);

        /**
         * Note that the order in which we specify the transitions matters since Hektor.io will run them in order
         * as specified. Hence, we know the {@link ConnectionActiveIOEvent} is an inbound event here because we previously
         * checked if it was outbound and apparently that didn't match so has to be inbound.
         */
        closed.transitionTo(WAIT_CER).onEvent(ConnectionActiveIOEvent.class);

        closed.transitionTo(TERMINATED).onEvent(String.class).withGuard("die"::equals);


        /**
         * state            event              action         next state
         * -----------------------------------------------------------------
         * R-Open           Send-Message     R-Snd-Message    R-Open
         *                  R-Rcv-Message    Process          R-Open
         *                  R-Rcv-DWR        Process-DWR,     R-Open
         *                                   R-Snd-DWA
         *                  R-Rcv-DWA        Process-DWA      R-Open
         *                  R-Conn-CER       R-Reject         R-Open
         *                  Stop             R-Snd-DPR        Closing
         *                  R-Rcv-DPR        R-Snd-DPA        Closing
         *                  R-Peer-Disc      R-Disc           Closed
         */
        open.transitionTo(OPEN).onEvent(DiameterMessage.class).withGuard(PeerFsm::isRetransmission).withAction(PeerFsm::handleRetransmission);
        open.transitionTo(OPEN).onEvent(DiameterMessage.class).withAction(PeerFsm::acceptAction);
        open.transitionTo(CLOSED).onEvent(String.class).withGuard("Disconnect"::equals);

        /**
         * state            event              action         next state
         * -----------------------------------------------------------------
         * Wait-Conn-Ack    I-Rcv-Conn-Ack   I-Snd-CER        Wait-I-CEA
         *                  I-Rcv-Conn-Nack  Cleanup          Closed
         *                  R-Conn-CER       R-Accept,        Wait-Conn-Ack/
         *                                   Process-CER      Elect
         *                  Timeout          Error            Closed
         */
        waitConnAck.transitionTo(OPEN).onEvent(ConnectionActiveIOEvent.class).withAction(PeerFsm::processRcvConnAck);

        /**
         * state            event              action         next state
         * -----------------------------------------------------------------
         * Wait-Cer         R-Conn-CER       R-Accept,        R-Open
         *                                   Process-CER,
         *                                   R-Snd-CEA
         *                  Timeout          Kill-Conn        Terminated
         */
        waitCer.transitionTo(OPEN).onEvent(DiameterMessage.class).withGuard(DiameterMessage::isCER).withAction(PeerFsm::processCER);
        // TODO: setup the timeout

        /**
         * state            event              action         next state
         * -----------------------------------------------------------------
         * Wait-I-CEA       I-Rcv-CEA        Process-CEA      I-Open
         *                  R-Conn-CER       R-Accept,        Wait-Returns
         *                                   Process-CER,
         *                                   Elect
         *                  I-Peer-Disc      I-Disc           Closed
         *                  I-Rcv-Non-CEA    Error            Closed
         *                  Timeout          Error            Closed
         */
        waitCea.transitionTo(OPEN).onEvent(DiameterMessage.class).withGuard(DiameterMessage::isCEA).withAction(PeerFsm::processCEA);
        waitCea.transitionTo(WAIT_CEA).onEvent(ConnectionAttemptCompletedIOEvent.class).withAction((evt, ctx, data) -> data.storeConnectionAttemptEvent(evt));

        definition = builder.build();
    }

    private static final boolean isRetransmission(final DiameterMessage msg, final PeerContext ctx, final PeerData data) {
        return data.hasOutstandingTransaction(msg);
    }

    private static final void handleRetransmission(final DiameterMessage msg, final PeerContext ctx, final PeerData data) {
        // TODO:
    }


    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // -------------------------- CLOSED STATE ------------------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    /**
     * According to spec, upon receiving a CER the FSM should perform the following three actions:
     * <ul>
     *     <li>R-Accept</li>
     *     <li>Process-CER</li>
     *     <li>R-Snd-CEA</li>
     * </ul>
     *
     * which is the responsibility of this method (well, accepting the CER I guess was done
     * as part of just calling this method!)
     */
    private static final void processCER(final DiameterMessage cer, final PeerContext ctx, final PeerData data) {
        // TODO: check if we want to accept traffic from the other peer.
        // TODO: check if we should check the applications and find the intersection of what we support or just accept all.

        final var builder = cer.createAnswer(ResultCode.DiameterSuccess2001);
        ctx.getHostIpAddresses().forEach(builder::withAvp);
        builder.withAvp(ctx.getConfig().getProductName());
        ctx.getChannelContext().sendDownstream(builder.build());
    }

    /**
     * When we establish an outbound connection and that connection successfully is established,
     * we must initiate the Capability Exchange procedure.
     */
    private static final void sendCer(final ConnectionActiveIOEvent event, final PeerContext ctx, final PeerData data) {
        final var cer = DiameterRequest.createCER()
                .withAvp(ctx.getProductName())
                .withOriginRealm(ctx.getOriginRealm())
                .withOriginHost(ctx.getOriginHost());
        ctx.getHostIpAddresses().forEach(ip -> cer.withAvp(ip));
        ctx.getChannelContext().sendDownstream(cer.build());
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // ---------------------------- OPEN STATE ------------------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // ------------------------- WAIT-CONN-ACK STATE ------------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    /**
     * Handles the RFC event "I-Rcv-Conn-Ack", which in the context of Snice Networking is a
     * {@link ConnectionActiveIOEvent} event.
     */
    private static final void processRcvConnAck(final ConnectionActiveIOEvent event, final PeerContext ctx, final PeerData data) {
        logger.info("Yay, connection established. Nedd to send the CER now...");
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // ------------------------------ WAIT-CEA ------------------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    private static final void processCEA(final DiameterMessage cea, final PeerContext ctx, final PeerData data) {
        logger.info("Received the CEA " + cea);

        // if this was a peer that was established by the user, then there may be
        // a waiting event stating that the underlying connection was successfully established
        // and now the peer is also happy so finally, let's tell the user that all things
        // are well and the peer is ready for use.
        data.consumeConnectionAttemptEvent().ifPresent(evt -> ctx.getChannelContext().fireUserEvent(evt));
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // ------------------------------ RFC6733 -------------------------------
    // ----------------------- Section: 5.6.3 Actions ----------------------
    // ----------------------------------------------------------------------

    /**
     * Action: Process
     * Description: A message is serviced
     * <p>
     * Quite simple, just push the message up the handler chain and eventually to the app.
     */
    private static final void acceptAction(final DiameterMessage msg, final PeerContext ctx, final PeerData data) {
        data.storeTransaction(msg);
        ctx.getChannelContext().sendUpstream(msg);
    }

}
