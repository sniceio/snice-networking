package io.snice.networking.diameter.peer.impl;

import io.hektor.fsm.FSM;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.avp.api.OriginHost;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.ConnectionEndpointId;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.common.fsm.FsmKey;
import io.snice.networking.common.fsm.FsmSupport;
import io.snice.networking.core.NetworkInterface;
import io.snice.networking.diameter.DiameterAppConfig;
import io.snice.networking.diameter.DiameterConfig;
import io.snice.networking.diameter.DiameterRoutingException;
import io.snice.networking.diameter.PeerConnection;
import io.snice.networking.diameter.event.DiameterEvent;
import io.snice.networking.diameter.peer.*;
import io.snice.networking.diameter.peer.fsm.PeerContext;
import io.snice.networking.diameter.peer.fsm.PeerData;
import io.snice.networking.diameter.peer.fsm.PeerFsm;
import io.snice.networking.diameter.peer.fsm.PeerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.snice.networking.common.Transport.tcp;

/**
 *
 */
public class DefaultPeerTable<C extends DiameterAppConfig> implements PeerTable<C> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPeerTable.class);
    private static final FsmSupport<PeerState> loggingSupport = new FsmSupport<>(PeerFsm.class);

    private final PeerConfiguration peerConfiguration = new PeerConfiguration();
    private final DiameterConfig config;
    private NetworkStack<PeerConnection, DiameterEvent, C> stack;
    private final RoutingEngine routingEngine;

    /**
     * TOOD: make the peer table configurable.
     */
    private final ConcurrentHashMap<PeerId, Peer> peers = new ConcurrentHashMap<>();

    public DefaultPeerTable(final DiameterConfig config, final RoutingEngine routingEngine) {
        this.config = config;
        this.routingEngine = routingEngine;
    }

    @Override
    public CompletionStage<PeerTable<C>> start(final NetworkStack<PeerConnection, DiameterEvent, C> stack) {
        this.stack = stack;
        config.getPeers().forEach(this::addPeer);
        return CompletableFuture.completedFuture(this);
    }

    @Override
    public void send(final DiameterMessage msg) throws DiameterRoutingException {
        routingEngine.findRoute(this, msg).or(() -> getDefaultPeer(msg)).orElseThrow(() -> {
            // TODO: need special exception. No peer found or something.
            throw new NoMatchingPeerException(msg);
        }).send(msg);
    }

    @Override
    public List<Peer> getPeers() {
        return peers.values().stream().collect(Collectors.toUnmodifiableList());
    }

    /**
     * If the {@link RoutingEngine} doesn't find an appropriate route for the given message, let's
     * pick a default route if we are configured/allowed to do so.
     */
    private Optional<Peer> getDefaultPeer(final DiameterMessage msg) {
        // for now, we'll just find any...
        return peers.entrySet().stream().findAny().map(Map.Entry::getValue);
    }

    @Override
    public FsmKey calculateKey(final ConnectionId connectionId, final Optional<DiameterEvent> evt) {
        final var originHost = evt.map(e -> e.toMessageEvent().getMessage()).map(DiameterMessage::getOriginHost);
        final var connectionEndpointId = connectionId.getRemoteConnectionEndpointId();
        return new PeerFsmKey(originHost, connectionEndpointId);
    }

    @Override
    public PeerData createNewDataBag(final FsmKey key) {
        return new PeerData(peerConfiguration);
    }

    @Override
    public PeerContext createNewContext(final FsmKey key, final ChannelContext<DiameterEvent> ctx) {
        final var peerCfg = peerConfiguration;
        final var peerCtx = new DefaultPeerContext(peerCfg, ctx, null);
        return peerCtx;
    }

    @Override
    public FSM<PeerState, PeerContext, PeerData> createNewFsm(final FsmKey key, final PeerContext ctx, final PeerData data) {
        final var fsm = PeerFsm.definition.newInstance(key, ctx, data, loggingSupport::unhandledEvent, loggingSupport::onTransition);
        return fsm;
    }

    @Override
    public Peer addPeer(final PeerConfiguration config) {
        logger.info("Adding Peer {}", config);
        final var nic = findNic(config);
        final var transport = ensureTransport(config.getTransport(), config.getName(), nic);
        final var settings = PeerSettings.of(config).withNetworkInterface(nic).withTransport(transport).build();
        final var peer = DefaultPeer.of(this, settings);
        peers.put(peer.getId(), peer);

        if (peer.getMode() == Peer.MODE.ACTIVE) {
            peer.establishPeer();
        }

        return peer;
    }

    /**
     * Try to find the configured {@link NetworkInterface} based on its friendly name.
     */
    private NetworkInterface findNic(final PeerConfiguration config) {
        final var interfaceName = config.getNic();
        final NetworkInterface nic;
        if (interfaceName.isPresent()) {
            nic = interfaceName.flatMap(stack::getNetworkInterface).orElseThrow(() -> {
                throw new IllegalArgumentException("The Peer named \"" + config.getName() + "\" is configured" +
                        " to use a Network Interface named \"" + interfaceName.get() + "\" but no such NIC has been" +
                        " configured. Please check your configuration.");
            });
        } else {
            nic = stack.getDefaultNetworkInterface();
        }

        return nic;
    }

    /**
     * Ensure that the configured {@link Transport} for the {@link Peer} is indeed supported by the given
     * {@link NetworkInterface}. If  the transport is not configured, try to find one that is supported by
     * the NIC.
     */
    private Transport ensureTransport(final Optional<Transport> transport, final String peerName, final NetworkInterface<DiameterMessage> nic) {
        if (transport.isPresent()) {
            final var t = transport.get();
            if (!nic.isSupportingTransport(t)) {
                throw new IllegalArgumentException("The configured transport \"" + t +
                        "\" for Peer \"" + peerName + "\" is not supported by the specified NIC \"" +
                        nic.getName() + "\"");
            }
            return t;
        } else if (nic.isSupportingTransport(tcp)) {
            return tcp;
        } else if (nic.isSupportingTransport(Transport.sctp)) {
            return Transport.sctp;
        } else {
            throw new IllegalArgumentException("No configured transport for Peer \"" + peerName +
                    "\" and the specified NIC \"" + nic.getName() +
                    "\" does not support TCP nor SCTP. Unable to create the Peer");
        }
    }

    CompletionStage<PeerConnection> activatePeer(final DefaultPeer peer) {
        logger.info("Activating Peer {}", config);
        // TODO: lots of error handling here!
        final var f = peer.resolveRemoteHost()
                .thenCompose(remoteAddress -> stack.connect(peer.getTransport(), remoteAddress))
                .thenApply(PeerConnection::of);
        return f;
    }

    @Override
    public CompletionStage<Peer> removePeer(final Peer peer, final boolean now) {
        return null;
    }

    private static class PeerFsmKey implements FsmKey {
        private final Optional<OriginHost> originHost;
        private final ConnectionEndpointId endpointId;

        private PeerFsmKey(final Optional<OriginHost> originHost, final ConnectionEndpointId endpointId) {
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
