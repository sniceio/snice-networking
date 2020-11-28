package io.snice.networking.gtp.impl;

import com.fasterxml.jackson.databind.Module;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.codecs.codec.gtp.gtpc.v2.Impl.Gtp2MessageBuilder;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.app.impl.GenericBootstrap;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.gtp.*;
import io.snice.networking.gtp.conf.ControlPlaneConfig;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.conf.UserPlaneConfig;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;
import io.snice.networking.gtp.fsm.GtpTunnelContext;
import io.snice.networking.gtp.fsm.GtpTunnelData;
import io.snice.networking.gtp.fsm.GtpTunnelState;
import io.snice.networking.gtp.handler.GtpMessageDatagramDecoder;
import io.snice.networking.gtp.handler.GtpMessageDatagramEncoder;
import io.snice.networking.netty.ProtocolHandler;
import io.snice.time.Clock;
import io.snice.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.snice.preconditions.PreConditions.assertNotNull;
import static io.snice.preconditions.PreConditions.ensureNotNull;

public class DefaultGtpStack<C extends GtpAppConfig> implements GtpStack<C> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultGtpStack.class);

    private final List<ProtocolHandler> encoders;
    private final List<ProtocolHandler> decoders;

    private final Clock clock = new SystemClock();

    private C configuration;

    private NetworkStack<Connection<GtpEvent>, GtpEvent, C> stack;
    private GtpTunnelFsmSupport gtpTunnelFsmSupport;
    private GtpSessionFsmFactory sessionFactory;

    private UserPlaneConfig userPlaneConfig;
    private ControlPlaneConfig controlPlaneConfig;

    private ConcurrentMap<ConnectionId, Connection<GtpEvent>> controlPlaneConnections;
    private ConcurrentMap<ConnectionId, Connection<GtpEvent>> userPlaneConnections;

    public DefaultGtpStack() {
        final var udpEncoder = ProtocolHandler.of("gtp-codec-encoder")
                .withChannelHandler(() -> new GtpMessageDatagramEncoder())
                .withTransport(Transport.udp)
                .build();
        encoders = List.of(udpEncoder);

        final var udpDecoder = ProtocolHandler.of("gtp-codec-decoder")
                .withChannelHandler(() -> new GtpMessageDatagramDecoder(clock))
                .withTransport(Transport.udp)
                .build();

        decoders = List.of(udpDecoder);
    }

    @Override
    public void initialize(final C config) {
        logger.info("Initializing GTP Stack");
        ensureNotNull(config, "The configuration object for the \"" + getBundleName() + "\" cannot be null");
        this.configuration = config;

        gtpTunnelFsmSupport = new GtpTunnelFsmSupport(config, clock);
        sessionFactory = new GtpSessionFsmFactory(config, clock);

        userPlaneConfig = config.getConfig().getUserPlane();
        userPlaneConnections = new ConcurrentHashMap<>(); // TODO: make the default size configurable

        controlPlaneConfig = config.getConfig().getControlPlane();
        controlPlaneConnections = new ConcurrentHashMap<>(); // TODO: make the default size configurable
    }

    /**
     * @param bootstrap
     */
    public final void initializeApplication(final GtpApplication<C> app, final NetworkBootstrap<GtpTunnel, GtpEvent, C> bootstrap) {
        final var gtpBootstrap = new GtpBootstrapImpl<>(bootstrap.getConfiguration());
        app.initialize((GtpBootstrap) gtpBootstrap);

        gtpBootstrap.getConnectionContexts().forEach(r -> {
            final var rule = r;
            final var b = bootstrap.onConnection(rule.getPredicate());
            if (rule.isDrop()) {
                b.drop(rule.getDropFunction().get());
            } else {
                b.accept(builder -> {
                    builder.match(e -> true).consume((peer, event) -> processEvent(rule, peer, event));
                });
            }
        });
    }

    private void processEvent(final ConnectionContext<GtpTunnel, GtpEvent> ctx, final GtpTunnel tunnel, final GtpEvent event) {
        if (event.isMessageReadEvent()) {
            // processMessageReadEvent(ctx, peer, event.toMessageReadEvent());
            System.err.println("Intercepted it!!!");
        } else {
            ctx.match(tunnel, event).apply(tunnel, event);
        }
    }

    @Override
    public String getBundleName() {
        return "GtpBundle";
    }

    @Override
    public Class<GtpEvent> getType() {
        return GtpEvent.class;
    }

    @Override
    public CompletionStage<ProtocolBundle<Connection<GtpEvent>, GtpEvent, C>> start(final NetworkStack<Connection<GtpEvent>, GtpEvent, C> stack) {
        logger.info("Starting GTP Stack");
        this.stack = stack;
        return CompletableFuture.completedFuture(this);
    }

    @Override
    public void stop() {
        logger.info("Stopping GTP Stack");
    }

    @Override
    public <E extends Environment<Connection<GtpEvent>, GtpEvent, C>> E createEnvironment(final NetworkStack<Connection<GtpEvent>, GtpEvent, C> stack, final C configuration) {
        return (E) new DefaultGtpEnvironment(this);
    }

    @Override
    public Optional<Module> getObjectMapModule() {
        return Optional.empty();
    }

    @Override
    public List<ProtocolHandler> getProtocolEncoders() {
        return encoders;
    }

    @Override
    public List<ProtocolHandler> getProtocolDecoders() {
        return decoders;
    }

    @Override
    public Connection<GtpEvent> wrapConnection(final Connection<GtpEvent> connection) {
        // TODO: may actually be either Control or User Plane. We need to match this
        // against which NIC it comes over.
        return DefaultGtpControlTunnel.of(connection.id(), this);
    }

    @Override
    public Optional<FsmFactory<GtpEvent, GtpTunnelState, GtpTunnelContext, GtpTunnelData>> getFsmFactory() {
        return Optional.of(gtpTunnelFsmSupport);
    }

    @Override
    public C getConfig() {
        return configuration;
    }

    @Override
    public void send(final GtpMessageWriteEvent event) {
        assertNotNull(event, "The event to send cannot be null");
        findConnection(event.getMessage(), event.getConnectionId()).send(event);
    }

    @Override
    public void send(final GtpMessage msg, final ConnectionId id) {
        System.err.println("Sending the gTP message");
        assertNotNull(msg, "The message to send cannot be null");
        assertNotNull(id, "The id of the connection to send the message over cannot be null");
        findConnection(msg, id).send(GtpMessageWriteEvent.of(msg, id));
    }

    private Connection<GtpEvent> findConnection(final GtpMessage msg, final ConnectionId id) {
        // TODO: not entirely correct but since we haven't implemented GTPv1 right now other than GTP-U it is correct for now.
        // Also, perhaps we should just put everything in one map and then a thin wrapper class/holder that
        // tells us if this is a GTP-U or GTP-C tunnel. And perhaps the version even though that is not really
        // something that is currently exposed as far as indicating what the tunnel will be used for (GTPv1 v.s. GTPv2 over the
        // control tunnel).
        final var connection = msg.isGtpVersion2() ? controlPlaneConnections.get(id) : userPlaneConnections.get(id);
        if (connection == null) {
            throw new IllegalArgumentException("There is no existing connection for " + id);
        }
        return connection;
    }

    @Override
    public void close(final ConnectionId connection) {
        throw new RuntimeException("Sorry, haven't implemented just yet");
    }

    @Override
    public CompletionStage<GtpControlTunnel> establishControlPlane(final InetSocketAddress remoteAddress) {
        if (!controlPlaneConfig.isEnable()) {
            throw new IllegalArgumentException("The Control Plane is not configured for this application");
        }

        assertNotNull(remoteAddress, "The remote address cannot be null");
        final var nic = stack.getNetworkInterface(controlPlaneConfig.getNic()).orElseThrow(() ->
                new IllegalArgumentException("Unable to find the Network Interface to use for the Control Plane. " +
                        "The configuration says to use \"" + controlPlaneConfig.getNic() + "\" but no such" +
                        "interface exists"));
        final var gtpStack = this;
        return nic.connect(Transport.udp, remoteAddress).thenApply(c -> {
            controlPlaneConnections.put(c.id(), c);
            return DefaultGtpControlTunnel.of(c.id(), gtpStack);
        });
    }

    @Override
    public CompletionStage<GtpUserTunnel> establishUserPlane(final InetSocketAddress remoteAddress) {
        if (!userPlaneConfig.isEnable()) {
            throw new IllegalArgumentException("The User Plane is not configured for this application");
        }
        assertNotNull(remoteAddress, "The remote address cannot be null");
        final var nic = stack.getNetworkInterface(userPlaneConfig.getNic()).orElseThrow(() ->
                new IllegalArgumentException("Unable to find the Network Interface to use for the User Plane. " +
                        "The configuration says to use \"" + userPlaneConfig.getNic() + "\" but no such" +
                        "interface exists"));
        final var gtpStack = this;
        return nic.connect(Transport.udp, remoteAddress).thenApply(c -> {
            userPlaneConnections.put(c.id(), c);
            return DefaultGtpUserTunnel.of(c.id(), gtpStack);
        });
    }

    @Override
    public PdnSession.Builder initiateNewPdnSession(final Gtp2Request createSessionRequest) {
        return null;
    }

    @Override
    public PdnSession.Builder initiateNewPdnSession(final Gtp2MessageBuilder<Gtp2Request> createSessionRequest) {
        return null;
    }

    private class GtpBootstrapImpl<C extends GtpAppConfig> extends GenericBootstrap<GtpTunnel, GtpEvent, C> implements GtpBootstrap<C> {
        public GtpBootstrapImpl(final C config) {
            super(config);
        }
    }
}
