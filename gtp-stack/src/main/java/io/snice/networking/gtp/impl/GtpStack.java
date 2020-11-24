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
import io.snice.networking.gtp.fsm.ControlTunnelFsm;
import io.snice.networking.gtp.fsm.GtpTunnelContext;
import io.snice.networking.gtp.fsm.GtpTunnelData;
import io.snice.networking.gtp.fsm.GtpTunnelState;
import io.snice.networking.gtp.fsm.impl.DefaultGtpTunnelContext;
import io.snice.time.Clock;

import java.util.Optional;

public class GtpStack implements FsmFactory<GtpEvent, GtpTunnelState, GtpTunnelContext, GtpTunnelData> {

    private static final FsmSupport<GtpTunnelState> loggingSupport = new FsmSupport<>(ControlTunnelFsm.class);

    private final GtpConfig config;
    private final Clock clock;

    public GtpStack(final GtpAppConfig config, final Clock clock) {
        this.config = config.getConfig();
        this.clock = clock;
    }

    @Override
    public FsmKey calculateKey(final ConnectionId connectionId, final Optional<GtpEvent> msg) {
        return ConnectionIdFsmKey.of(connectionId);
    }

    @Override
    public GtpTunnelData createNewDataBag(final FsmKey key) {
        return GtpTunnelData.of(config);
    }

    @Override
    public GtpTunnelContext createNewContext(final FsmKey key, final ChannelContext<GtpEvent> ctx) {
        return DefaultGtpTunnelContext.of(config, ctx);
    }

    @Override
    public FSM<GtpTunnelState, GtpTunnelContext, GtpTunnelData> createNewFsm(final FsmKey key, final GtpTunnelContext ctx, final GtpTunnelData data) {
        return ControlTunnelFsm.definition.newInstance(key, ctx, data, loggingSupport::unhandledEvent, loggingSupport::onTransition);
    }
}
