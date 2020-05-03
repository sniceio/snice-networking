package io.snice.networking.app.impl;

import com.fasterxml.jackson.databind.Module;
import io.hektor.fsm.Data;
import io.netty.channel.ChannelHandler;
import io.snice.networking.app.AppBundle;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkStack;
import io.snice.codecs.codec.SerializationFactory;
import io.snice.networking.common.Connection;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.common.fsm.NetworkContext;
import io.snice.networking.netty.NettyNetworkLayer;
import io.snice.networking.netty.ProtocolHandler;
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
public class NettyNetworkStack<T, C extends NetworkAppConfig> implements NetworkStack<T, C> {

    private final Class<T> type;
    private final SerializationFactory<T> serializationFactory;
    private final C config;
    private final NetworkApplication<T, C> app;
    private final List<ConnectionContext> ctxs;
    private NettyNetworkLayer network;
    private final Clock clock = new SystemClock();
    private final AppBundle<T> appBundle;

    private NettyNetworkStack(final Class<T> type,
                              final C config,
                              final SerializationFactory<T> framerFactory,
                              final NetworkApplication<T, C> app,
                              final AppBundle<T> appBundle,
                              final List<ConnectionContext> ctxs) {
        this.type = type;
        this.serializationFactory = framerFactory;
        this.config = config;
        this.app = app;
        this.appBundle = appBundle;
        this.ctxs = ctxs;
    }

    public static <T> FramerFactoryStep<T> ofType(final Class<T> type) {
        assertNotNull(type, "The type cannot be null");
        return framerFactory -> {
            // assertNotNull(framerFactory, "The Framer Factory cannot be null");
            return new ConfigurationStep<T>() {
                @Override
                public <C extends NetworkAppConfig> Builder<T, C> withConfiguration(final C config) {
                    assertNotNull(config, "The configuration for the network stack cannot be null");
                    return new Builder(type, framerFactory, config);
                }
            };

        };

    }

    @Override
    public void start() {


        final var appLayer = new NettyApplicationLayer();
        // final var diameterConf = new DiameterConfig();
        // final var fsmFactory = PeerFactory.createDefault(diameterConf);

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
        network = NettyNetworkLayer.with(config.getNetworkInterfaces())

                .withHandler(appBundle.getProtocolEncoders())
                .withHandler(appBundle.getProtocolDecoders())
                // .withHandler("diameter-codec-encoder", () -> new DiameterStreamEncoder(), Transport.tcp)
                // .withHandler("diameter-code-decoder", () -> new DiameterMessageStreamDecoder2(), Transport.tcp)

                .withHandler("udp-adapter", () -> new NettyUdpInboundAdapter(clock, serializationFactory, Optional.empty(), ctxs), Transport.udp)
                .withHandler("tcp-adapter", () -> new NettyTcpInboundAdapter(clock, serializationFactory, Optional.empty(), ctxs), Transport.tcp)
                // .withHandler("tcp-adapter-outbound", () -> new NettyTcpOutboundAdapter<>(clock, serializationFactory, Optional.empty(), ctxs), Transport.tcp)

                // the optional fsm layer - will also be injected dynamically depending on whether
                // the user actually wants an FSM layer or not.
                // Also, whether there is a separate FSM layer per connection or a shared
                // one for the entire stack will be dependent on the actual need of the implementation.

                // TODO: need to handle when it is not configured.
                .withHandler("fsm-layer", () -> new NettyFsmLayer(appBundle.getFsmFactory().get()), Transport.tcp)

                // App layer is not optional so it will always be injected but it will need to be configured by
                // the NetworkApplication etc in order to inject the message pipelines and whatnot.
                .withHandler("app-layer", () -> appLayer, Transport.tcp)
                .withHandler("app-layer", () -> appLayer, Transport.udp)
                .build();
        network.start();
        appBundle.start();
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
    public void stop() {
        network.stop();
    }

    private static class Builder<T, C extends NetworkAppConfig> implements NetworkStack.Builder<T, C> {

        private final Class<T> type;
        private final SerializationFactory<T> serializationFactory;
        private final C config;
        private NetworkApplication application;
        private List<ConnectionContext> ctxs;
        private AppBundle<T> appBundle;

        private Builder(final Class<T> type, final SerializationFactory<T> serializationFactory, final C config) {
            this.type = type;
            this.serializationFactory = serializationFactory;
            this.config = config;
        }

        @Override
        public NetworkStack.Builder<T, C> withApplication(final NetworkApplication<T, C> application) {
            assertNotNull(application, "The application cannot be null");
            this.application = application;
            return this;
        }

        @Override
        public NetworkStack.Builder<T, C> withAppBundle(final AppBundle<T> bundle) {
            assertNotNull(bundle, "The application bundle cannot be null");
            this.appBundle = bundle;
            return this;
        }


        @Override
        public NetworkStack.Builder<T, C> withConnectionContexts(final List<ConnectionContext> ctxs) {
            assertArgument(ctxs != null && !ctxs.isEmpty(), "You cannot have a null or empty list of " + ConnectionContext.class.getSimpleName());
            this.ctxs = ctxs;
            return this;
        }

        @Override
        public NetworkStack<T, C> build() {
            ensureNotNull(application, "You must specify the Sip Application");
            final var bundle = appBundle != null ? appBundle : new EmptyAppBundle<T>(type);
            return new NettyNetworkStack(type, config, serializationFactory, application, bundle, ctxs);
        }
    }

    private static class EmptyAppBundle<T> implements AppBundle<T> {
        private final Class<T> type;

        private EmptyAppBundle(final Class<T> type) {
            this.type = type;
        }


        @Override
        public Class<T> getType() {
            return type;
        }

        @Override
        public Optional<Module> getObjectMapModule() {
            return Optional.empty();
        }

        @Override
        public List<ProtocolHandler> getProtocolEncoders() {
            return List.of();
        }

        @Override
        public List<ProtocolHandler> getProtocolDecoders() {
            return List.of();
        }

        @Override
        public <S extends Enum<S>, C extends NetworkContext<T>, D extends Data> Optional<FsmFactory<T, S, C, D>> getFsmFactory() {
            return Optional.empty();
        }
    }
}
