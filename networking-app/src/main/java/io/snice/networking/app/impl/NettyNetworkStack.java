package io.snice.networking.app.impl;

import io.netty.channel.ChannelHandler;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.common.Transport;
import io.snice.networking.netty.NettyNetworkLayer;
import io.snice.time.Clock;
import io.snice.time.SystemClock;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertArray;
import static io.snice.preconditions.PreConditions.assertNotNull;
import static io.snice.preconditions.PreConditions.ensureNotNull;

@ChannelHandler.Sharable
public class NettyNetworkStack implements NetworkStack {

    private final NetworkAppConfig config;
    private final NetworkApplication app;
    private final List<ConnectionContext> ctxs;
    private NettyNetworkLayer network;
    private final Clock clock = new SystemClock();

    private NettyNetworkStack(final NetworkAppConfig config, final NetworkApplication app, final List<ConnectionContext> ctxs) {
        this.config = config;
        this.app = app;
        this.ctxs = ctxs;
    }

    public static Builder withConfig(final NetworkAppConfig config) {
        assertNotNull(config, "The configuration cannot be null");
        return new Builder(config);
    }

    @Override
    public void start() {
        network = NettyNetworkLayer.with(config.getNetworkInterfaces())
                .withHandler("udp-adapter", () -> new NettyUdpInboundAdapter(clock, Optional.empty(), ctxs), Transport.udp)
                .withHandler("tcp-adapter", () -> new NettyTcpInboundAdapter(clock, Optional.empty(), ctxs), Transport.tcp)
                .build();
        network.start();
    }

    @Override
    public CompletionStage<Void> sync() {
        return network.sync();
    }

    @Override
    public void stop() {
        network.stop();

    }

    private static class Builder implements NetworkStack.Builder<NetworkAppConfig> {

        private final NetworkAppConfig config;
        private NetworkApplication application;
        private List<ConnectionContext> ctxs;

        private Builder(final NetworkAppConfig config) {
            this.config = config;
        }

        @Override
        public NetworkStack.Builder withApplication(final NetworkApplication application) {
            assertNotNull(application, "The application cannot be null");
            this.application = application;
            return this;
        }


        @Override
        public NetworkStack.Builder withConnectionContexts(final List<ConnectionContext> ctxs) {
            assertArgument(ctxs != null && !ctxs.isEmpty(), "You cannot have a null or empty list of " + ConnectionContext.class.getSimpleName());
            this.ctxs = ctxs;
            return this;
        }

        @Override
        public NetworkStack build() {
            ensureNotNull(application, "You must specify the Sip Application");
            return new NettyNetworkStack(config, application, ctxs);
        }
    }
}
