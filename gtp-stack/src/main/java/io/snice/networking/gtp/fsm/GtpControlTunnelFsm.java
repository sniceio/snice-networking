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

        closed.transitionTo(SYNC).onEvent(ConnectionActiveIOEvent.class).withGuard(ConnectionActiveIOEvent::isOutboundConnection);
        sync.transitionTo(OPEN).onEvent(ConnectionAttemptCompletedIOEvent.class).withAction(GtpControlTunnelFsm::processConnectionCompleted);
        open.transitionTo(OPEN).onEvent(GtpMessageReadEvent.class).withGuard(GtpMessageReadEvent::isEchoRequest).withAction(GtpControlTunnelFsm::processEchoRequest);
        open.transitionTo(OPEN).onEvent(GtpMessageReadEvent.class).withAction(GtpControlTunnelFsm::processRead);
        open.transitionTo(OPEN).onEvent(GtpMessageWriteEvent.class).withAction((evt, tunnel, data) -> tunnel.sendDownstream(evt));


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

    private static final void processRead(final GtpMessageReadEvent event, final GtpTunnelContext ctx, final GtpTunnelData data) {
        ctx.sendUpstream(event);
    }
}
