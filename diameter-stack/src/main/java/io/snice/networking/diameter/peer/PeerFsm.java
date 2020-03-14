package io.snice.networking.diameter.peer;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;
import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.avp.api.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.snice.networking.diameter.peer.PeerState.CLOSED;
import static io.snice.networking.diameter.peer.PeerState.OPEN;
import static io.snice.networking.diameter.peer.PeerState.TERMINATED;

public class PeerFsm {

    private static final Logger logger = LoggerFactory.getLogger(PeerFsm.class);

    public static final Definition<PeerState, PeerContext, PeerData> definition;

    static {
        final var builder = FSM.of(PeerState.class).ofContextType(PeerContext.class).withDataType(PeerData.class);
        final var closed = builder.withInitialState(CLOSED);
        final var open = builder.withState(OPEN);
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
         * state            event              action         next state
         * -----------------------------------------------------------------
         * Closed           Start            I-Snd-Conn-Req   Wait-Conn-Ack
         *                  R-Conn-CER       R-Accept,        R-Open
         *                                   Process-CER,
         *                                   R-Snd-CEA
         *
         *
         */
        closed.transitionTo(OPEN).onEvent(DiameterMessage.class).withGuard(DiameterMessage::isCER).withAction(PeerFsm::processCER);
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

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // ---------------------------- OPEN STATE ------------------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

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
