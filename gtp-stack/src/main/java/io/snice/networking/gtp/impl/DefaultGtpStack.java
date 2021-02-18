package io.snice.networking.gtp.impl;

import com.fasterxml.jackson.databind.Module;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Response;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.net.IPv4;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.app.impl.GenericBootstrap;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionEndpointId;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.core.NetworkInterface;
import io.snice.networking.gtp.DataTunnel;
import io.snice.networking.gtp.EpsBearer;
import io.snice.networking.gtp.GtpApplication;
import io.snice.networking.gtp.GtpBootstrap;
import io.snice.networking.gtp.GtpControlTunnel;
import io.snice.networking.gtp.GtpTunnel;
import io.snice.networking.gtp.GtpUserTunnel;
import io.snice.networking.gtp.IllegalGtpMessageException;
import io.snice.networking.gtp.PdnSession;
import io.snice.networking.gtp.PdnSessionContext;
import io.snice.networking.gtp.Transaction;
import io.snice.networking.gtp.conf.ControlPlaneConfig;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.conf.UserPlaneConfig;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageReadEvent;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.snice.preconditions.PreConditions.assertNotNull;
import static io.snice.preconditions.PreConditions.ensureNotNull;

public class DefaultGtpStack<C extends GtpAppConfig> implements InternalGtpStack<C> {

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

    /**
     * If we are configured to handle GTP-U, this will be the network interface
     * we are doing it over.
     */
    private NetworkInterface<GtpEvent> gtpuNic;

    private NetworkInterface<GtpEvent> gtpcNic;

    /**
     * Keep track of all tunnels based on to where they are "connected".
     * <p>
     * Note that since we only support a single NIC per tunnel "type" (so GTP-U v.s.
     * GTP-C) it is safe to only use the remote endpoint as the key into this map.
     * If that were to change, we would have to change this.
     */
    private ConcurrentMap<ConnectionEndpointId, Connection<GtpEvent>> gtpuTunnels;

    private ConcurrentMap<ConnectionEndpointId, Connection<GtpEvent>> gtpcTunnels;

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
        controlPlaneConfig = config.getConfig().getControlPlane();

        gtpcTunnels = new ConcurrentHashMap<>(controlPlaneConfig.getInitialTunnelStoreSize());
        gtpuTunnels = new ConcurrentHashMap<>(userPlaneConfig.getInitialTunnelStoreSize());
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
                final var saveFunction = rule.getSaveAction();
                b.save(tunnel -> saveInboundConnection(tunnel, saveFunction));
                b.accept(builder -> {
                    builder.match(e -> true).consume((peer, event) -> processEvent(rule, peer, event));
                });
            }
        });
    }

    private void saveInboundConnection(final GtpTunnel tunnel, final Optional<Consumer<GtpTunnel>> userSaveFunction) {
        userSaveFunction.ifPresent(f -> f.accept(tunnel));
    }

    private void processEvent(final ConnectionContext<GtpTunnel, GtpEvent> ctx, final GtpTunnel tunnel, final GtpEvent event) {
        if (event.isMessageReadEvent()) {
            processMessageReadEvent(ctx, tunnel, event.toMessageReadEvent());
        } else {
            ctx.match(tunnel, event).apply(tunnel, event);
        }
    }

    private void processMessageReadEvent(final ConnectionContext<GtpTunnel, GtpEvent> ctx, final GtpTunnel tunnel, final GtpMessageReadEvent event) {
        final var transactionMaybe = event.getTransaction();

        // TODO: this was a hack to try some stuff out. Should allow for a message pipe thing
        // TODO: also, what if a transaction is present but e.g no onAnswer callback has been
        // TODO: specified, do we fallback to the general context?
        if (transactionMaybe.isPresent()) {
            final var transaction = transactionMaybe.get();
            final var msg = event.getMessage();
            if (msg.isResponse()) {
                final var callback = transaction.getOnResponse();
                if (callback != null) {
                    callback.accept(transaction, msg.toGtp2Response());
                }
            }
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

        if (userPlaneConfig.isEnable()) {
            gtpuNic = stack.getNetworkInterface(userPlaneConfig.getNic()).orElseThrow(() ->
                    new IllegalArgumentException("Unable to find the Network Interface to use for the User Plane. " +
                            "The configuration says to use \"" + userPlaneConfig.getNic() + "\" but no such " +
                            "interface exists"));
        }

        if (controlPlaneConfig.isEnable()) {
            gtpcNic = stack.getNetworkInterface(controlPlaneConfig.getNic()).orElseThrow(() ->
                    new IllegalArgumentException("Unable to find the Network Interface to use for the Control Plane. " +
                            "The configuration says to use \"" + controlPlaneConfig.getNic() + "\" but no such " +
                            "interface exists"));
        }

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
        // Note: you cannot SAVE this connection for future use, ever!
        // The reason is that the underlying network layer needs to be in control and will
        // typically for incoming messages wrap a fake connection so it buffers up all
        // messages that are to be processed after the invocation of the application
        // returns...
        return DirectGtpControlTunnel.of(connection, this);
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
    public void send(final GtpMessageWriteEvent event, final InternalGtpControlTunnel tunnel) {
        assertNotNull(event, "The event to send cannot be null");
        findConnection(event.getMessage(), event.getConnectionId()).send(event);
    }


    @Override
    public void send(final GtpMessage msg, final InternalGtpControlTunnel tunnel) {
        assertNotNull(msg, "The message to send cannot be null");
        assertNotNull(tunnel, "The GTP Control Tunnel cannot be null");
        findConnection(msg, tunnel.id()).send(GtpMessageWriteEvent.of(msg, tunnel.id()));
    }

    @Override
    public void send(final GtpMessage msg, final InternalGtpUserTunnel tunnel) {
        // TODO: come up with a better idea of doing this. Good to have the separation
        // of the "real" tunnel but these lookups all the time seems un-necessary.
        // Perhaps a InternalGtpUserTunnel.getActualTunnel()
        findConnection(msg, tunnel.id()).send(GtpMessageWriteEvent.of(msg, tunnel.id()));
    }

    @Override
    public Transaction.Builder createNewTransaction(final InternalGtpControlTunnel tunnel, final Gtp2Request request) throws IllegalGtpMessageException {
        return DefaultTransaction.of(tunnel, this, request);
    }

    @Override
    public <T> DataTunnel.Builder<T> createDataTunnel(final InternalGtpUserTunnel tunnel, final Class<T> type, final String remoteHost, final int port) {
        return null;
    }

    private Connection<GtpEvent> findConnection(final GtpMessage msg, final ConnectionId id) {
        final var remoteEndpoint = id.getRemoteConnectionEndpointId();
        final Connection<GtpEvent> connection;
        if (msg.isGtpVersion2()) {
            connection = gtpcTunnels.get(remoteEndpoint);
        } else {
            // TODO: not entirely correct but we only support GTP-U right now so good enough
            connection = gtpuTunnels.get(remoteEndpoint);
        }

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
        final var gtpStack = this;
        return gtpcNic.connect(Transport.udp, remoteAddress).thenApply(c -> {
            // System.err.println("Saving GTP-C tunnel " + c.id());
            gtpcTunnels.put(c.id().getRemoteConnectionEndpointId(), c);
            return DefaultGtpControlTunnel.of(c.id(), gtpStack);
        });
    }

    @Override
    public GtpUserTunnel establishUserPlane(final InetSocketAddress remoteAddress) {
        if (!userPlaneConfig.isEnable()) {
            throw new IllegalArgumentException("The User Plane is not configured for this application");
        }
        assertNotNull(remoteAddress, "The remote address cannot be null");

        final var remoteConnectionId = ConnectionEndpointId.create(Transport.udp, remoteAddress);
        final var tunnel = gtpuTunnels.computeIfAbsent(remoteConnectionId, key -> gtpuNic.connectDirect(Transport.udp, remoteAddress));

        // TODO: to make things more efficient, perhaps should actually store the "actual" tunnel
        // after all. the findConnection of freaking everything seems annoying...
        return DefaultGtpUserTunnel.of(tunnel.id(), this);
    }

    @Override
    public PdnSession.Builder<C> initiateNewPdnSession(final CreateSessionRequest createSessionRequest) {
        assertNotNull(createSessionRequest, "The Create Session Request cannot be null");
        return new PdnSessionBuilder<C>(createSessionRequest);
    }

    private class GtpBootstrapImpl<C extends GtpAppConfig> extends GenericBootstrap<GtpTunnel, GtpEvent, C> implements GtpBootstrap<C> {
        public GtpBootstrapImpl(final C config) {
            super(config);
        }
    }

    /**
     * Used when a user wish the stack to manage the {@link PdnSession}s for them and this method is
     * called when the initial response to the Create Session Request is received.
     */
    private void processPdnSessionResponse(final Transaction transaction, final Gtp2Response response) {
        final PdnSessionInitialTransactionHolder holder = (PdnSessionInitialTransactionHolder) transaction.getApplicationData().get();
        final var ctx = PdnSessionContext.of(holder.csr, response);
        final var session = new DefaultPdnSession<C>(ctx, holder.tunnel);
        holder.future.complete(session);
    }

    /**
     * Used when a user asks to {@link PdnSession#terminate()} and we will trap the response in this method and
     * kill the session.
     */
    private void processPdnSessionTerminatedResponse(final Transaction transaction, final Gtp2Response response) {
        final var session = (DefaultPdnSession<C>) transaction.getApplicationData().get();
        // TODO: have to invoke any potential onSessionTerminated callbacks...
    }

    private class PdnSessionBuilder<C extends GtpAppConfig> implements PdnSession.Builder {

        private final CreateSessionRequest csr;
        private String remoteAddress;
        private int port = 2123;

        private PdnSessionBuilder(final CreateSessionRequest csr) {
            this.csr = csr;
        }

        @Override
        public PdnSession.Builder withRemoteIPv4(final String ipv4) {
            IPv4.fromString(ipv4); // will make sure it is correct
            remoteAddress = ipv4;
            return this;
        }

        @Override
        public PdnSession.Builder withRemotePort(final int port) {
            this.port = port;
            return this;
        }

        @Override
        public PdnSession.Builder onSessionTerminated(final BiConsumer f) {
            throw new RuntimeException("not yet implemented");
        }

        @Override
        public CompletionStage<PdnSession> start() {
            assertNotNull(remoteAddress, "The address of the remote element (such as a PGW) cannot be null");
            final CompletableFuture<PdnSession> future = new CompletableFuture<>();
            establishControlPlane(new InetSocketAddress(remoteAddress, port)).thenAccept(tunnel -> {
                tunnel.createNewTransaction(csr)
                        .withApplicationData(new PdnSessionInitialTransactionHolder(tunnel, future, csr))
                        .onAnswer(DefaultGtpStack.this::processPdnSessionResponse)
                        .start();
            }).exceptionally(throwable -> {
                // TODO: need to handle this better. For now, good enough;
                future.completeExceptionally(throwable);
                return null;
            });
            return future;
        }
    }

    private class DefaultPdnSession<C extends GtpAppConfig> implements PdnSession {

        private final int defaultGtpuPort = 2152;
        private final PdnSessionContext ctx;
        private final GtpControlTunnel tunnel;

        private DefaultPdnSession(final PdnSessionContext ctx, final GtpControlTunnel tunnel) {
            this.ctx = ctx;
            this.tunnel = tunnel;
        }

        @Override
        public PdnSessionContext getContext() {
            return ctx;
        }

        @Override
        public void terminate() {
            tunnel.createNewTransaction(ctx.createDeleteSessionRequest().build().toGtp2Request())
                    .withApplicationData(this)
                    .onAnswer(DefaultGtpStack.this::processPdnSessionTerminatedResponse)
                    .start();
        }

        @Override
        public EpsBearer establishDefaultBearer() {
            final var localPort = 7893;
            final var remoteBearer = ctx.getDefaultRemoteBearer();
            // System.err.println("The IP Address in the remote bearer is: " + remoteBearer.getIPv4AddressAsString().get());
            final var pgw = "127.0.0.1"; // TODO: have to fix this NAT issue.
            final var remote = new InetSocketAddress(pgw, defaultGtpuPort);
            final var tunnel = (InternalGtpUserTunnel) establishUserPlane(remote);
            // TODO: need to save it away too...
            return DefaultEpsBearer.create(tunnel, ctx, localPort);
        }
    }



    private static class PdnSessionInitialTransactionHolder {
        private final GtpControlTunnel tunnel;
        public final CreateSessionRequest csr;
        public final CompletableFuture<PdnSession> future;

        private PdnSessionInitialTransactionHolder(final GtpControlTunnel tunnel, final CompletableFuture<PdnSession> future, final CreateSessionRequest csr) {
            this.tunnel = tunnel;
            this.csr = csr;
            this.future = future;
        }
    }

}
