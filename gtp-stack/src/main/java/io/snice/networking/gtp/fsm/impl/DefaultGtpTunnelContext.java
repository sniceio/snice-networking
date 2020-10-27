package io.snice.networking.gtp.fsm.impl;

import io.hektor.fsm.Scheduler;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.networking.common.ChannelContext;
import io.snice.networking.gtp.conf.GtpConfig;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageReadEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;
import io.snice.networking.gtp.fsm.GtpTunnelContext;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultGtpTunnelContext implements GtpTunnelContext {

    private final GtpConfig config;
    private final ChannelContext<GtpEvent> ctx;

    public static GtpTunnelContext of(final GtpConfig config, final ChannelContext<GtpEvent> ctx) {
        assertNotNull(config);
        assertNotNull(ctx);
        return new DefaultGtpTunnelContext(config, ctx);
    }

    private DefaultGtpTunnelContext(final GtpConfig config, final ChannelContext<GtpEvent> ctx) {
        this.config = config;
        this.ctx = ctx;
    }

    @Override
    public void sendDownstream(final Gtp2Message msg) {
        final var evt = GtpMessageWriteEvent.of(msg, ctx.getConnectionId());
        ctx.sendDownstream(evt);
    }

    @Override
    public void sendDownstream(final GtpEvent msg) {
        ctx.sendDownstream(msg);
    }

    @Override
    public void sendUpstream(final Gtp2Message msg) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void sendUpstream(final GtpEvent event) {
        ctx.sendUpstream(event);
    }

    @Override
    public ChannelContext<GtpEvent> getChannelContext() {
        return ctx;
    }

    @Override
    public Scheduler getScheduler() {
        return null;
    }
}
