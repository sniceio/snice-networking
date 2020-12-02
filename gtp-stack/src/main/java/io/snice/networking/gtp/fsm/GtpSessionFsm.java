package io.snice.networking.gtp.fsm;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.snice.networking.gtp.fsm.GtpSessionState.INIT;
import static io.snice.networking.gtp.fsm.GtpSessionState.TERMINATED;
import static io.snice.networking.gtp.fsm.GtpTunnelState.CLOSED;

public class GtpSessionFsm {

    private static final Logger logger = LoggerFactory.getLogger(GtpControlTunnelFsm.class);

    public static final Definition<GtpSessionState, GtpSessionContext, GtpSessionData> definition;

    static {

        final var builder = FSM.of(GtpSessionState.class).ofContextType(GtpSessionContext.class).withDataType(GtpSessionData.class);

        final var init = builder.withInitialState(INIT);
        final var terminated = builder.withFinalState(TERMINATED);

        init.transitionTo(TERMINATED).onEvent(String.class);

        definition = builder.build();
    }
}
