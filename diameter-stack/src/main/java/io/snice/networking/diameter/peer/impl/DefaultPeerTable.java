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
import io.snice.networking.diameter.PeerConnection;
import io.snice.networking.diameter.peer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
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
    private NetworkStack<PeerConnection, DiameterMessage, C> stack;
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
    public CompletionStage<PeerTable<C>> start(final NetworkStack<PeerConnection, DiameterMessage, C> stack) {
        this.stack = stack;
        config.getPeers().forEach(this::addPeer);
        return CompletableFuture.completedFuture(this);
    }

    @Override
    public void send(final DiameterMessage msg) {
        routingEngine.findRoute(this, msg).or(() -> getDefaultPeer(msg)).orElseThrow(() -> {
            // TODO: need special exception. No peer found or something.
            throw new RuntimeException("Unable to find Peer for the given message");
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
    public FsmKey calculateKey(final ConnectionId connectionId, final Optional<DiameterMessage> msg) {
        final var originHost = msg.map(DiameterMessage::getOriginHost);
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

    @Override
    public Peer addPeer(final PeerConfiguration config) {
        // TODO: I guess we need to actually start a connection attempt here if need be?
        // or should the PeerFsm always be created here and it'll take care of it
        // based on its configuration options.
        // but I guess for e.g. passive peers, we may not have the connection endpoint id
        // yet and as such, we can't create the PeerFsmKey...
        logger.info("Adding Peer {}", config);
        // TODO: we should actually error out if the given network interface name doesn't exist.
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

        final var peer = DefaultPeer.of(config);
        peers.put(peer.getId(), peer);

        if (config.getMode() == Peer.MODE.ACTIVE) {
            activatePeer(config, nic).thenAccept(peer::setConnection);
        }

        return peer;
    }

    private CompletionStage<PeerConnection> activatePeer(final PeerConfiguration config, final NetworkInterface nic) {
        logger.info("Activating Peer {}", config);

        final Transport transport;
        if (config.getTransport().isPresent()) {
            final var t = config.getTransport().get();
            if (!nic.isSupportingTransport(t)) {
                throw new IllegalArgumentException("The configured transport \"" + t +
                        "\" for Peer \"" + config.getName() + "\" is not supported by the specified NIC \"" +
                        nic.getName() + "\"");
            }
            transport = t;
        } else if (nic.isSupportingTransport(tcp)) {
            transport = tcp;
        } else if (nic.isSupportingTransport(Transport.sctp)) {
            transport = Transport.sctp;
        } else {
            throw new IllegalArgumentException("No configured transport for Peer \"" + config.getName() +
                    "\" and the specified NIC \"" + nic.getName() +
                    "\" does not support TCP nor SCTP. Unable to create the Peer");
        }

        // TODO: figure out the remote Address
        final var uri = config.getUri();
        final var host = uri.getHost();
        final var port = uri.getPort() == -1 ? getDefaultPort(transport) : uri.getPort();

        // final var remoteAddress = InetSocketAddress.createUnresolved(host, port);
        // really do want to create a unresolved address here and only if need be we'll
        // do a DNS lookup but we need to control that ourselves.
        // See comment in ConnectionId.decode about this.
        final var remoteAddress = new InetSocketAddress(host, port);
        return stack.connect(transport, remoteAddress).thenApply(PeerConnection::of);
    }

    private static int getDefaultPort(final Transport transport) {
        switch (transport) {
            case tcp:
                return 3869;
            default:
                return 3868;
        }
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
