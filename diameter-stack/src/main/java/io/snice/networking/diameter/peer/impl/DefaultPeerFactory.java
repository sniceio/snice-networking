package io.snice.networking.diameter.peer.impl;

import io.hektor.fsm.FSM;
import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.avp.api.OriginHost;
import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.ConnectionEndpointId;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.fsm.FsmKey;
import io.snice.networking.common.fsm.FsmSupport;
import io.snice.networking.diameter.DiameterConfig;
import io.snice.networking.diameter.peer.PeerConfiguration;
import io.snice.networking.diameter.peer.PeerContext;
import io.snice.networking.diameter.peer.PeerData;
import io.snice.networking.diameter.peer.PeerFactory;
import io.snice.networking.diameter.peer.PeerFsm;
import io.snice.networking.diameter.peer.PeerState;

import java.util.Optional;

/**
 * Perhaps this is the Peer table instead?
 */
public class DefaultPeerFactory implements PeerFactory {

    private static final FsmSupport<PeerState> loggingSupport = new FsmSupport<>(PeerFsm.class);
    private final PeerConfiguration peerConfiguration = new PeerConfiguration();
    private final DiameterConfig config;

    public DefaultPeerFactory(final DiameterConfig config) {
        this.config = config;
    }

    @Override
    public FsmKey calculateKey(final ConnectionId connectionId, final Optional<DiameterMessage> msg) {
        final var originHost = msg.get().getOriginHost();
        final var connectionEndpointId = connectionId.getRemoteConnectionEndpointId();
        return new PeerFsmKey(originHost, connectionEndpointId);
    }

    @Override
    public PeerData createNewDataBag(final FsmKey key) {
        return new PeerData(peerConfiguration);
    }

    @Override
    public PeerContext createNewContext(final FsmKey key, final ChannelContext<DiameterMessage> ctx) {
        final var peerCfg = peerConfiguration;
        final var peerCtx = new DefaultPeerContext(peerCfg, ctx, null);
        return peerCtx;
    }

    @Override
    public FSM<PeerState, PeerContext, PeerData> createNewFsm(final FsmKey key, final PeerContext ctx, final PeerData data) {
        final var fsm = PeerFsm.definition.newInstance(key, ctx, data, loggingSupport::unhandledEvent, loggingSupport::onTransition);
        return fsm;
    }

    private static class PeerFsmKey implements FsmKey {
        private final OriginHost originHost;
        private final ConnectionEndpointId endpointId;

        private PeerFsmKey(final OriginHost originHost, final ConnectionEndpointId endpointId) {
            this.originHost = originHost;
            this.endpointId = endpointId;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final PeerFsmKey that = (PeerFsmKey) o;
            if (!originHost.equals(that.originHost)) return false;
            return endpointId.equals(that.endpointId);
        }

        @Override
        public int hashCode() {
            return originHost.hashCode() + 31 * endpointId.hashCode();
        }
    }

}
