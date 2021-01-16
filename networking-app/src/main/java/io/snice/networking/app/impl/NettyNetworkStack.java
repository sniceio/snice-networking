package io.snice.networking.app.impl;

import io.netty.channel.ChannelHandler;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;
import io.snice.networking.core.NetworkInterface;
import io.snice.networking.netty.NettyNetworkLayer;
import io.snice.time.Clock;
import io.snice.time.SystemClock;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertNotNull;
import static io.snice.preconditions.PreConditions.ensureNotNull;

@ChannelHandler.Sharable
public class NettyNetworkStack<E extends Environment<K, T, C>, K extends Connection<T>, T, C extends NetworkAppConfig> implements NetworkStack<K, T, C> {

    private final C config;
    private final NetworkApplication<E, K, T, C> app;
    private final List<ConnectionContext<K, T>> ctxs;
    private NettyNetworkLayer network;
    private final Clock clock = new SystemClock();
    private final ProtocolBundle<K, T, C> protocolBundle;

    private NettyNetworkStack(final C config,
                              final NetworkApplication<E, K, T, C> app,
                              final ProtocolBundle<K, T, C> protocolBundle,
                              final List<ConnectionContext<K, T>> ctxs) {
        this.config = config;
        this.app = app;
        this.protocolBundle = protocolBundle;
        this.ctxs = ctxs;
    }

    public static <E extends Environment<K, T, C>, K extends Connection<T>, T, C extends NetworkAppConfig> Builder<E, K, T, C> ofConfiguration(final C config) {
        return new Builder(config);
    }

    @Override
    public void start() {
        final var appLayer = new NettyApplicationLayer(protocolBundle);

        // TODO: the network bundle should probably be loaded by looking at the
        // TODO: schema of the listening addresses.
        // TODO: so perhaps something like:
        // TODO: var ifConfig = config.getNetworkInterfaces().get(xxx);
        // TODO: var protocolStackBundle = ProtocolManager.find(ifConfig.getListeningAddress().getSchema()).orElseThrow("No protocol stack configured for schema xxxx");
        // TODO: as opposed to now where the appBundle is passed into the constructor. We want to be able
        // TODO: to have multiple stacks... hence, there whould be many 'network'
        // TODO: so perhaps
        // TODO: config.getNetworkInterfaces().stream().groupBy(schema).collect();
        // TODO: and then
        final var builder = NettyNetworkLayer.with(config.getNetworkInterfaces())
                .withHandler(protocolBundle.getProtocolEncoders())
                .withHandler(protocolBundle.getProtocolDecoders())
                .withHandler("udp-adapter", () -> new NettyUdpInboundAdapter(clock, Optional.empty(), ctxs), Transport.udp)
                .withHandler("tcp-adapter", () -> new NettyTcpInboundAdapter(clock, Optional.empty(), ctxs), Transport.tcp)
                .withHandler("sctp-adapter", () -> new NettySctpInboundAdapter(clock, Optional.empty(), ctxs), Transport.sctp);

        // the optional fsm layer - will also be injected dynamically depending on whether
        // the user actually wants an FSM layer or not.
        // Also, whether there is a separate FSM layer per connection or a shared
        // one for the entire stack will be dependent on the actual need of the implementation.
        // TODO: need to ensure that the FSM factory is for a particular transport too
        protocolBundle.getFsmFactory().ifPresent(fsmFactory -> {
            builder.withHandler("fsm-layer", () -> new NettyFsmLayer(fsmFactory), Transport.udp);
            builder.withHandler("fsm-layer", () -> new NettyFsmLayer(fsmFactory), Transport.tcp);
            builder.withHandler("fsm-layer", () -> new NettyFsmLayer(fsmFactory), Transport.sctp);
        });

        // App layer is not optional so it will always be injected but it will need to be configured by
        // the NetworkApplication etc in order to inject the message pipelines and whatnot.
        network = builder
                .withHandler("app-layer", () -> appLayer, Transport.udp)
                .withHandler("app-layer", () -> appLayer, Transport.tcp)
                .withHandler("app-layer", () -> appLayer, Transport.sctp)
                .build();

        network.start();
    }

    @Override
    public CompletionStage<Void> sync() {
        return network.sync();
    }

    @Override
    public CompletionStage<Connection<T>> connect(final Transport transport, final InetSocketAddress remoteAddress) throws IllegalTransportException {
        return network.connect(transport, remoteAddress);
    }

    @Override
    public CompletionStage<Connection<T>> connect(final Transport transport, final int localPort, final InetSocketAddress remoteAddress) throws IllegalTransportException {
        return network.connect(transport, localPort, remoteAddress);
    }


    @Override
    public CompletionStage<NetworkInterface<T>> addNetworkInterface(final Transport transport) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public NetworkInterface<T> getDefaultNetworkInterface() {
        return network.getDefaultNetworkInterface();
    }

    @Override
    public Optional<NetworkInterface<T>> getNetworkInterface(final String interfaceName, final Transport transport) {
        return network.getNetworkInterface(interfaceName, transport);
    }

    @Override
    public Optional<NetworkInterface<T>> getNetworkInterface(final String interfaceName) {
        return network.getNetworkInterface(interfaceName);
    }

    @Override
    public List<NetworkInterface<T>> getNetworkInterfaces(final Transport transport) {
        return network.getNetworkInterfaces(transport);
    }

    @Override
    public void stop() {
        network.stop();
    }

    private static class Builder<E extends Environment<K, T, C>, K extends Connection<T>, T, C extends NetworkAppConfig> implements NetworkStack.Builder<E, K, T, C> {

        private final C config;
        private NetworkApplication<E, K, T, C> application;
        private List<ConnectionContext<K, T>> ctxs;
        private ProtocolBundle<K, T, C> protocolBundle;

        private Builder(final C config) {
            this.config = config;
        }

        @Override
        public NetworkStack.Builder<E, K, T, C> withApplication(final NetworkApplication<E, K, T, C> application) {
            assertNotNull(application, "The application cannot be null");
            this.application = application;
            return this;
        }

        @Override
        public NetworkStack.Builder<E, K, T, C> withAppBundle(final ProtocolBundle<K, T, C> bundle) {
            assertNotNull(bundle, "The application bundle cannot be null");
            this.protocolBundle = bundle;
            return this;
        }

        @Override
        public NetworkStack.Builder<E, K, T, C> withConnectionContexts(final List<ConnectionContext<K, T>> ctxs) {
            assertArgument(ctxs != null && !ctxs.isEmpty(), "You cannot have a null or empty list of " + ConnectionContext.class.getSimpleName());
            this.ctxs = ctxs;
            return this;
        }

        @Override
        public NetworkStack<K, T, C> build() {
            ensureNotNull(application, "You must specify the actual Application");
            ensureNotNull(protocolBundle, "You must specify the application bundle");
            return new NettyNetworkStack(config, application, protocolBundle, ctxs);
        }
    }

}
