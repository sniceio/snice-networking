package io.snice.networking.diameter.peer.fsm;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;
import io.snice.codecs.codec.diameter.DiameterAnswer;
import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.codecs.codec.diameter.avp.api.*;
import io.snice.networking.common.event.ConnectionActiveIOEvent;
import io.snice.networking.common.event.ConnectionAttemptCompletedIOEvent;
import io.snice.networking.common.event.ConnectionConnectAttemptIOEvent;
import io.snice.networking.diameter.event.DiameterMessageEvent;
import io.snice.networking.diameter.event.DiameterMessageReadEvent;
import io.snice.networking.diameter.event.DiameterMessageWriteEvent;
import io.snice.networking.diameter.tx.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.snice.networking.diameter.peer.fsm.PeerState.*;

public class PeerFsm {

    private static final Logger logger = LoggerFactory.getLogger(PeerFsm.class);

    public static final Definition<PeerState, PeerContext, PeerData> definition;

    static {
        final var builder = FSM.of(PeerState.class).ofContextType(PeerContext.class).withDataType(PeerData.class);
        final var closed = builder.withInitialState(CLOSED);
        final var open = builder.withState(OPEN);
        final var closing = builder.withState(CLOSING);
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
         * state            event              action         next state   implemented
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
        closed.transitionTo(WAIT_CER).onEvent(ConnectionActiveIOEvent.class).withAction(PeerFsm::processConnectionActive);

        /**
         * Temp - hektor.io will evaluate the FSM to ensure you can reach the final state in some
         * way and if not, it'll complain.
         */
        closed.transitionTo(TERMINATED).onEvent(String.class).withGuard("die"::equals);


        /**
         * state            event              action         next state   implemented
         * ------------------------------------------------------------------------------
         * R-Open           Send-Message     R-Snd-Message    R-Open
         *                  R-Rcv-Message    Process          R-Open           x
         *                  R-Rcv-DWR        Process-DWR,     R-Open           x
         *                                   R-Snd-DWA
         *                  R-Rcv-DWA        Process-DWA      R-Open
         *                  R-Conn-CER       R-Reject         R-Open
         *                  Stop             R-Snd-DPR        Closing
         *                  R-Rcv-DPR        R-Snd-DPA        Closing           x
         *                  R-Peer-Disc      R-Disc           Closed
         */
        open.transitionTo(OPEN).onEvent(DiameterMessageEvent.class).withGuard(PeerFsm::isRetransmission).withAction(PeerFsm::handleRetransmission);
        open.transitionTo(OPEN).onEvent(DiameterMessageEvent.class).withGuard(DiameterMessageEvent::isDWR).withAction(PeerFsm::processDWR);
        open.transitionTo(CLOSING).onEvent(DiameterMessageEvent.class).withGuard(DiameterMessageEvent::isDPR).withAction(PeerFsm::processDPR);
        open.transitionTo(OPEN).onEvent(DiameterMessageReadEvent.class).withAction(PeerFsm::processRead);
        open.transitionTo(OPEN).onEvent(DiameterMessageWriteEvent.class).withAction(PeerFsm::processWrite);
        open.transitionTo(CLOSED).onEvent(String.class).withGuard("Disconnect"::equals);

        /**
         * state            event              action         next state    implemented
         * ----------------------------------------------------------------------------
         * Wait-Conn-Ack    I-Rcv-Conn-Ack   I-Snd-CER        Wait-I-CEA
         *                  I-Rcv-Conn-Nack  Cleanup          Closed
         *                  R-Conn-CER       R-Accept,        Wait-Conn-Ack/
         *                                   Process-CER      Elect
         *                  Timeout          Error            Closed
         */
        waitConnAck.transitionTo(OPEN).onEvent(ConnectionActiveIOEvent.class).withAction(PeerFsm::processRcvConnAck);

        /**
         * state            event              action         next state    implemented
         * ----------------------------------------------------------------------------
         * Wait-Cer         R-Conn-CER       R-Accept,        R-Open
         *                                   Process-CER,
         *                                   R-Snd-CEA
         *                  Timeout          Kill-Conn        Terminated
         */
        waitCer.transitionTo(OPEN).onEvent(DiameterMessageEvent.class).withGuard(DiameterMessageEvent::isCER).withAction(PeerFsm::processCER);
        // TODO: setup the timeout

        /**
         * state            event              action         next state    implemented
         * ----------------------------------------------------------------------------
         * Wait-I-CEA       I-Rcv-CEA        Process-CEA      I-Open
         *                  R-Conn-CER       R-Accept,        Wait-Returns
         *                                   Process-CER,
         *                                   Elect
         *                  I-Peer-Disc      I-Disc           Closed
         *                  I-Rcv-Non-CEA    Error            Closed
         *                  Timeout          Error            Closed
         */
        waitCea.transitionTo(OPEN).onEvent(DiameterMessageEvent.class).withGuard(DiameterMessageEvent::isCEA).withAction(PeerFsm::processCEA);
        waitCea.transitionTo(WAIT_CEA).onEvent(ConnectionAttemptCompletedIOEvent.class).withAction((evt, ctx, data) -> data.storeConnectionAttemptEvent(evt));

        /**
         * state            event              action         next state    implemented
         * ----------------------------------------------------------------------------
         * Closing          I-Rcv-DPA        I-Disc           Closed
         *                  R-Rcv-DPA        R-Disc           Closed
         *                  Timeout          Error            Closed
         *                  I-Peer-Disc      I-Disc           Closed
         *                  R-Peer-Disc      R-Disc           Closed
         */
        closing.transitionTo(CLOSED).onEvent(String.class).withGuard("TODO"::equals);

        definition = builder.build();
    }

    private static final boolean isRetransmission(final DiameterMessageEvent evt, final PeerContext ctx, final PeerData data) {
        final var msg = evt.getMessage();
        if (evt.isMessageWriteEvent() && msg.isAnswer()) {
            return false;
        }

        final var transaction = data.getTransaction(msg);

        if (transaction == null) {
            return false;
        }

        // If we receive an answer and we are the ones that initiated the transaction
        // by sending the request, then this is not a re-transmission but simply
        // an answer to our outstanding transaction. Let it through.
        // NOTE: this needs to be enhanced because we could receive the same Answer again
        // and at that point, we shouldn't allow it up to the app. This could e.g. happen
        // when we re-transmit the request and just as we did so, the answer shows up. Or
        // if someone is mean and attacking us...
        if (msg.isAnswer() && transaction.isClientTransaction()) {
            return false;
        }

        // TODO: lots more to do here...
        return transaction != null;
    }

    private static final void handleRetransmission(final DiameterMessageEvent evt, final PeerContext ctx, final PeerData data) {
        // TODO:
    }


    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // -------------------------- CLOSED STATE ------------------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    /**
     * When the underlying network stack has accepted a connection (TCP, UDP, SCTP, whatever) that event
     * will propagate up the stack and eventually be given to this FSM. Since we need to do the CER/CEA
     * handshake, we will "hold onto" this event until we have successfully established the Peer and
     * at that point, we will propagate that event again. Hence, all this method does is to save the
     * event.
     */
    private static final void processConnectionActive(final ConnectionActiveIOEvent evt, final PeerContext ctx, final PeerData data) {
        data.storeConnectionActiveIoEvent(evt);
    }

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
    private static final void processCER(final DiameterMessageEvent cer, final PeerContext ctx, final PeerData data) {
        // TODO: check if we want to accept traffic from the other peer.
        // TODO: check if we should check the applications and find the intersection of what we support or just accept all.

        final var builder = cer.getRequest().createAnswer(ResultCode.DiameterSuccess2001);
        ctx.getHostIpAddresses().forEach(builder::withAvp);
        ctx.getConfig().getProductName().ifPresent(builder::withAvp);
        builder.withOriginRealm(ctx.getOriginRealm());
        builder.withOriginHost(ctx.getOriginHost());
        ctx.sendDownstream(builder.build());

        // should we perhaps create "PeerConnection" here?
        data.consumeConnectionActiveEvent().ifPresent(evt -> ctx.getChannelContext().fireUserEvent(evt));
    }

    /**
     * When we establish an outbound connection and that connection successfully is established,
     * we must initiate the Capability Exchange procedure.
     */
    private static final void sendCer(final ConnectionActiveIOEvent event, final PeerContext ctx, final PeerData data) {
        final var cer = DiameterRequest.createCER()
                .withOriginRealm(ctx.getOriginRealm())
                .withOriginHost(ctx.getOriginHost());
        ctx.getProductName().ifPresent(cer::withAvp);
        ctx.getHostIpAddresses().forEach(ip -> cer.withAvp(ip));

        final var vendorId = VendorId.of(10415L);
        final var authId = AuthApplicationId.of(16777251L);
        final var acctId = AcctApplicationId.of(0L);
        final var app = VendorSpecificApplicationId.of(vendorId, authId, acctId);
        cer.withAvp(app);

        // get the support apps from the peer context
        final var c = cer.build();
        ctx.sendDownstream(c);
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // ---------------------------- OPEN STATE ------------------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    /**
     * On a watch-dog message, simply create an answer.
     */
    private static final void processDWR(final DiameterMessageEvent evt, final PeerContext ctx, final PeerData data) {
        final var dwr = evt.getRequest();
        final var dwa = dwr.createAnswer(ResultCode.DiameterSuccess2001)
                .withOriginRealm(ctx.getOriginRealm())
                .withOriginHost(ctx.getOriginHost())
                .build();

        ctx.sendDownstream(dwa);
    }

    /**
     * On a disconnect request, reply back and let the app know (TODO: not done yet)
     *
     * TODO: if the result is REBOOTING we should/could try and re-establish the connection.
     * TODO: we should add that to the state machine on the CLOSED state.
     *
     * Also, we need to, after sending the DPA, wait for the remote  party to either disconnect
     * or if they don't, we need to do so after we are ensure the DPA has been sent.
     *
     * Note that we are missing the "connection disconnected" event from the NettyTcpInboundAdapter
     * so that needs to get done...
     */
    private static final void processDPR(final DiameterMessageEvent evt, final PeerContext ctx, final PeerData data) {
        final var dpr = evt.getRequest();
        final var dpa = dpr.createAnswer(ResultCode.DiameterSuccess2001)
                .withOriginRealm(ctx.getOriginRealm())
                .withOriginHost(ctx.getOriginHost())
                .build();

        final var cause = dpr.getAvp(DisconnectCause.CODE)
                .map(c -> ((DisconnectCause) c.ensure()))
                .orElse(DisconnectCause.DoNotWantToTalkToYou) // if not present, should we complain?
                .getAsEnum().get();

        switch (cause) {
            case REBOOTING:
                break;
            case BUSY:
                break;
            case DO_NOT_WANT_TO_TALK_TO_YOU:
                break;
        }

        ctx.sendDownstream(dpa);
    }

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
        logger.info("Yay, connection established. Need to send the CER now...");
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // ------------------------------ WAIT-CEA ------------------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    private static final void processCEA(final DiameterMessageEvent evt, final PeerContext ctx, final PeerData data) {
        // TODO: ensure that the CEA is success.
        // TODO: handle e.g. 5010 - Diameter no common application etc.
        final var cea = evt.getAnswer();
        final var result = cea.getResultCode().get().toResultCode();
        final var code = result.getAsEnum().get();

        // if this was a peer that was established by the user, then there may be
        // a waiting event stating that the underlying connection was successfully established
        // and now the peer is also happy so finally, let's tell the user that all things
        // are well and the peer is ready for use.
        final var connectionAttemptEvt = data.consumeConnectionAttemptEvent().map(e -> {
            if (code == ResultCode.Code.DIAMETER_NO_COMMON_APPLICATION_5010) {
                return e.fail(new RuntimeException("No Common Diameter Application with remote peer"));
            }
            return e;
        });

        connectionAttemptEvt.ifPresent(e -> ctx.getChannelContext().fireUserEvent(e));
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
     * When a new message has been read off of the network there are two options. If it is a request
     * it may be a re-transmission (already checked so can't actually be that here) or it is a new
     * request.
     *
     * If a new request, then we'll create a "transaction holder" and pass it up to the application,
     * who may later on choose to "care" about the transaction and therefore may end up creating a {@link Transaction}
     * object (which really isn't the "real" transaction, it's simply a set of user defined callbacks and whatnot).
     * That user defined transaction will then later show up in a "answer" event and will be dealt with in the write
     * section.
     *
     * If it is an {@link DiameterAnswer} there must be an existing {@link InternalTransaction} or we simply
     * did not initiate this transaction by sending a request. Currently being dropped as bogus. Should
     * allow user to define what should happen. Ideas: Drop & log, special "StrayAnswerEvent" and let the application
     * deal with it.
     */
    private static final void processRead(final DiameterMessageEvent evt, final PeerContext ctx, final PeerData data) {
        final var msg = evt.getMessage();
        if (msg.isRequest()) {
            data.storeTransaction(msg.toRequest(), false);
            ctx.getChannelContext().sendUpstream(evt);
            return;
        }

        final var transaction = data.getTransaction(msg);
        if (transaction == null) {
            logger.info("Dropping stray Answer {}", msg);
            return;
        }

        final var decoratedEventMaybe = transaction.getTransaction()
                .map(t -> DiameterMessageReadEvent.of(msg, t))
                .orElse(evt.toMessageReadEvent());
        ctx.getChannelContext().sendUpstream(decoratedEventMaybe);
    }

    /**
     * Action: Process
     * Description: A message is serviced
     * <p>
     *
     */
    private static final void processWrite(final DiameterMessageEvent evt, final PeerContext ctx, final PeerData data) {
        final var msg = evt.getMessage();
        final var userTransaction = evt.getTransaction();

        if (msg.isRequest()) {
            final var transaction = data.storeTransaction(msg.toRequest(), true);
            userTransaction.ifPresent(transaction::setTransaction);
        } else {
            final var transaction = data.getTransaction(msg);
            if (transaction != null) {
                userTransaction.ifPresent(transaction::setTransaction);
            }
            // TODO: if the application sends an answer and we have not transaction
            // it means we didn't actually see a request for it. Should we allow
            // the app to do so or not? For now, we'll allow it...
            // Use case could be the app is a tester and tests other stacks and as such,
            // we should allow it. (perhaps there is a mode: Tester? IgnoreRfc?)
            // Issue though is if we do send the Answer due to us timing out and purging
            // the transaction, the other side may think this is a re-transmission, re-transmits
            // the original request, which we will not treat as a new request.
        }

        ctx.getChannelContext().sendDownstream(evt);
    }

}
