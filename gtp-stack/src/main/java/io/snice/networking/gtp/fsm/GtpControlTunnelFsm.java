package io.snice.networking.gtp.fsm;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.path.EchoRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Recovery;
import io.snice.networking.common.event.ConnectionActiveIOEvent;
import io.snice.networking.common.event.ConnectionAttemptCompletedIOEvent;
import io.snice.networking.gtp.event.GtpMessageReadEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.snice.networking.gtp.fsm.GtpTunnelState.*;

public class GtpControlTunnelFsm {

    private static final Logger logger = LoggerFactory.getLogger(GtpControlTunnelFsm.class);

    public static final Definition<GtpTunnelState, GtpTunnelContext, GtpTunnelData> definition;

    static {
        final var builder = FSM.of(GtpTunnelState.class).ofContextType(GtpTunnelContext.class).withDataType(GtpTunnelData.class);
        final var closed = builder.withInitialState(CLOSED);
        final var sync = builder.withState(SYNC);
        final var open = builder.withState(OPEN);
        final var terminated = builder.withFinalState(TERMINATED);

        closed.transitionTo(SYNC).onEvent(ConnectionActiveIOEvent.class);
        sync.transitionTo(OPEN).onEvent(ConnectionAttemptCompletedIOEvent.class).withAction(GtpControlTunnelFsm::processConnectionCompleted);
        open.transitionTo(OPEN).onEvent(GtpMessageReadEvent.class).withGuard(GtpMessageReadEvent::isEchoRequest).withAction(GtpControlTunnelFsm::processEchoRequest);
        open.transitionTo(OPEN).onEvent(GtpMessageReadEvent.class).withAction(GtpControlTunnelFsm::processRead);
        open.transitionTo(OPEN).onEvent(GtpMessageWriteEvent.class).withAction(GtpControlTunnelFsm::processWrite);


        // TODO: need to figure out what kills it. For now, just so that the FSM actually builds.
        // TODO: I believe there is a "termination" packet to send to the remote endpoint too...
        open.transitionTo(TERMINATED).onEvent(String.class).withGuard("die"::equals);

        definition = builder.build();
    }

    /**
     * If the UDP "connection" attempt is successful (and it'll always be since there is no real connection per se,
     * nor do we attempt to e.g. send a EchoRequest to see if the other end is an actual GTP-C node), just
     * forward the event up the chain and another layer will take care of completing the user future within.
     */
    private static final void processConnectionCompleted(final ConnectionAttemptCompletedIOEvent event, final GtpTunnelContext ctx, final GtpTunnelData data) {
        ctx.getChannelContext().fireUserEvent(event);
    }

    private static final void processEchoRequest(final GtpMessageReadEvent event, final GtpTunnelContext ctx, final GtpTunnelData data) {
        final var echo = (EchoRequest) event.getMessage().toGtp2Request();
        final var echoResponse = echo.createResponse().withTliv(Recovery.ofValue("7")).build();
        ctx.sendDownstream(echoResponse);
    }

    /**
     * Process an incoming read event.
     * <p>
     * Note that any re-transmissions should already have been dealt with so that is why we do not
     * need to check that here...
     */
    private static final void processRead(final GtpMessageReadEvent event, final GtpTunnelContext ctx, final GtpTunnelData data) {
        final var msg = event.getMessage();

        if (msg.isGtpVersion1()) {
            ctx.sendUpstream(event);
            return;
        }

        // TODO: need to change because currently Gtp2Request doesn't extent GtpRequest. Must have missed something.
        if (msg.isRequest()) {
            final var transaction = data.storeTransaction(msg.toGtp2Request(), false);
            event.getTransaction().ifPresent(transaction::setTransaction);
            ctx.getChannelContext().sendUpstream(event);
            return;
        }

        // not actually correct but I haven't scheduled any timers just yet
        // so we would leak these...
        final var transaction = data.removeTransaction(msg);
        if (transaction == null) {
            logger.info("Dropping stray response {}", msg);
            return;
        }

        final var decoratedEventMaybe = transaction.getTransaction()
                .map(t -> GtpMessageReadEvent.of(msg, t))
                .orElse(event);
        ctx.sendUpstream(decoratedEventMaybe);
    }

    private static final void processWrite(final GtpMessageWriteEvent event, final GtpTunnelContext ctx, final GtpTunnelData data) {
        // TODO: need to change because currently Gtp2Request doesn't extent GtpRequest. Must have missed something.
        final var msg = event.getMessage();
        if (msg.isGtpVersion2()) {
            if (msg.isRequest()) {
                final var transaction = data.storeTransaction(msg, true);
                event.getTransaction().ifPresent(transaction::setTransaction);
            } else {
                final var transaction = data.removeTransaction(msg);
                if (transaction == null) {
                    logger.warn("Odd, got a GTPv2 response that didn't match a transaction");
                }
            }
        }

        ctx.sendDownstream(event);
    }
}
