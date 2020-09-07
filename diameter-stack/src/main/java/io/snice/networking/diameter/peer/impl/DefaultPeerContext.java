package io.snice.networking.diameter.peer.impl;

import io.hektor.fsm.Scheduler;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.avp.api.HostIpAddress;
import io.snice.networking.common.ChannelContext;
import io.snice.networking.diameter.event.DiameterEvent;
import io.snice.networking.diameter.event.DiameterMessageReadEvent;
import io.snice.networking.diameter.event.DiameterMessageWriteEvent;
import io.snice.networking.diameter.peer.PeerConfiguration;
import io.snice.networking.diameter.peer.fsm.PeerContext;

import java.util.List;

public class DefaultPeerContext implements PeerContext {

    private final PeerConfiguration peerConfig;
    private final ChannelContext<DiameterEvent> channelContext;
    private final Scheduler scheduler;

    public DefaultPeerContext(final PeerConfiguration config,
                              final ChannelContext<DiameterEvent> channelContext,
                              final Scheduler scheduler) {
        this.peerConfig = config;
        this.channelContext = channelContext;
        this.scheduler = scheduler;
    }

    @Override
    public PeerConfiguration getConfig() {
        return peerConfig;
    }

    @Override
    public void sendDownstream(final DiameterMessage msg) {
        final var evt = DiameterMessageWriteEvent.of(msg);
        channelContext.sendDownstream(evt);
    }

    @Override
    public void sendUpstream(final DiameterMessage msg) {
        final var evt = DiameterMessageReadEvent.of(msg);
        channelContext.sendUpstream(evt);
    }

    @Override
    public List<HostIpAddress> getHostIpAddresses() {
        return peerConfig.getHostIpAddresses();
    }

    @Override
    public ChannelContext<DiameterEvent> getChannelContext() {
        return channelContext;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }
}
