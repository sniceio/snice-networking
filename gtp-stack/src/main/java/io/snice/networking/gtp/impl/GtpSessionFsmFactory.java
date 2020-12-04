package io.snice.networking.gtp.impl;

import io.hektor.fsm.FSM;
import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.fsm.ConnectionIdFsmKey;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.common.fsm.FsmKey;
import io.snice.networking.common.fsm.FsmSupport;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.conf.GtpConfig;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.fsm.GtpSessionContext;
import io.snice.networking.gtp.fsm.GtpSessionData;
import io.snice.networking.gtp.fsm.GtpSessionFsm;
import io.snice.networking.gtp.fsm.GtpSessionState;
import io.snice.networking.gtp.fsm.impl.DefaultGtpSessionContext;
import io.snice.time.Clock;

import java.util.Optional;

public class GtpSessionFsmFactory implements FsmFactory<GtpEvent, GtpSessionState, GtpSessionContext, GtpSessionData> {

    private static final FsmSupport<GtpSessionState> loggingSupport = new FsmSupport<>(GtpSessionFsm.class);

    private final GtpConfig config;
    private final Clock clock;

    public GtpSessionFsmFactory(final GtpAppConfig config, final Clock clock) {
        this.config = config.getConfig();
        this.clock = clock;
    }

    @Override
    public FsmKey calculateKey(final ConnectionId connectionId, final Optional<GtpEvent> msg) {
        if (!msg.isPresent()) {
            return null;
        }

        return ConnectionIdFsmKey.of(connectionId);
    }

    @Override
    public GtpSessionData createNewDataBag(final FsmKey key) {
        return GtpSessionData.of(config);
    }

    @Override
    public GtpSessionContext createNewContext(final FsmKey key, final ChannelContext<GtpEvent> ctx) {
        return DefaultGtpSessionContext.of(config, ctx);
    }

    @Override
    public FSM<GtpSessionState, GtpSessionContext, GtpSessionData> createNewFsm(final FsmKey key, final GtpSessionContext ctx, final GtpSessionData data) {
        return GtpSessionFsm.definition.newInstance(key, ctx, data, loggingSupport::unhandledEvent, loggingSupport::onTransition);
    }
}
