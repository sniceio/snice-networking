package io.snice.networking.app.impl;

import io.netty.channel.ChannelHandler;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.codec.SerializationFactory;
import io.snice.networking.common.Connection;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;
import io.snice.networking.diameter.peer.PeerFactory;
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
public class NettyNetworkStack<T, C extends NetworkAppConfig> implements NetworkStack<T, C> {

    private final Class<T> type;
    private final SerializationFactory<T> serializationFactory;
    private final C config;
    private final NetworkApplication<T, C> app;
    private final List<ConnectionContext> ctxs;
    private NettyNetworkLayer network;
    private final Clock clock = new SystemClock();

    private NettyNetworkStack(final Class<T> type, final C config, final SerializationFactory<T> framerFactory, final NetworkApplication<T, C> app, final List<ConnectionContext> ctxs) {
        this.type = type;
        this.serializationFactory = framerFactory;
        this.config = config;
        this.app = app;
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
        final var fsmFactory = PeerFactory.createDefault();

        network = NettyNetworkLayer.with(config.getNetworkInterfaces())
                // TODO: the encoder/decoder will be injected by the application but for now, while working out the details,
                // i'm doing it manually here...
                .withHandler("diameter-codec-encoder", () -> new DiameterStreamEncoder(), Transport.tcp)
                .withHandler("diameter-code-decoder", () -> new DiameterMessageStreamDecoder2(), Transport.tcp)

                .withHandler("udp-adapter", () -> new NettyUdpInboundAdapter(clock, serializationFactory, Optional.empty(), ctxs), Transport.udp)
                .withHandler("tcp-adapter", () -> new NettyTcpInboundAdapter(clock, serializationFactory, Optional.empty(), ctxs), Transport.tcp)

                // the optional fsm layer - will also be injected dynamically depending on whether
                // the user actually wants an FSM layer or not.
                // Also, whether there is a separate FSM layer per connection or a shared
                // one for the entire stack will be dependent on the actual need of the implementation.
                .withHandler("fsm-layer", () -> new NettyFsmLayer(fsmFactory), Transport.tcp)

                // App layer is not optional so it will always be injected but it will need to be configured by
                // the NetworkApplication etc in order to inject the message pipelines and whatnot.
                .withHandler("app-layer", () -> appLayer, Transport.tcp)
                .withHandler("app-layer", () -> appLayer, Transport.udp)
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
    public void stop() {
        network.stop();
    }

    private static class Builder<T, C extends NetworkAppConfig> implements NetworkStack.Builder<T, C> {

        private final Class<T> type;
        private final SerializationFactory<T> serializationFactory;
        private final C config;
        private NetworkApplication application;
        private List<ConnectionContext> ctxs;

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
        public NetworkStack.Builder<T, C> withConnectionContexts(final List<ConnectionContext> ctxs) {
            assertArgument(ctxs != null && !ctxs.isEmpty(), "You cannot have a null or empty list of " + ConnectionContext.class.getSimpleName());
            this.ctxs = ctxs;
            return this;
        }

        @Override
        public NetworkStack<T, C> build() {
            ensureNotNull(application, "You must specify the Sip Application");
            return new NettyNetworkStack(type, config, serializationFactory, application, ctxs);
        }
    }
}
