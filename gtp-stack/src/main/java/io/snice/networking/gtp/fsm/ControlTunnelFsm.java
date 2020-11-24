package io.snice.networking.gtp.fsm;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;
import io.snice.networking.common.event.ConnectionActiveIOEvent;
import io.snice.networking.common.event.ConnectionAttemptCompletedIOEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.snice.networking.gtp.fsm.GtpTunnelState.*;

public class ControlTunnelFsm {

    private static final Logger logger = LoggerFactory.getLogger(ControlTunnelFsm.class);

    public static final Definition<GtpTunnelState, GtpTunnelContext, GtpTunnelData> definition;

    static {
        final var builder = FSM.of(GtpTunnelState.class).ofContextType(GtpTunnelContext.class).withDataType(GtpTunnelData.class);
        final var closed = builder.withInitialState(CLOSED);
        final var sync = builder.withState(SYNC);
        final var open = builder.withState(OPEN);
        final var terminated = builder.withFinalState(TERMINATED);

        closed.transitionTo(SYNC).onEvent(ConnectionActiveIOEvent.class).withGuard(ConnectionActiveIOEvent::isOutboundConnection);
        sync.transitionTo(OPEN).onEvent(ConnectionAttemptCompletedIOEvent.class).withAction(ControlTunnelFsm::processConnectionCompleted);


        // TODO: need to figure out what kills it. For now, just so that the FSM actually builds.
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
}
