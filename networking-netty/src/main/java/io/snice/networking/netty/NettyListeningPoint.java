package io.snice.networking.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionEndpointId;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;
import io.snice.networking.core.ListeningPoint;
import io.snice.networking.core.event.ConnectionAttempt;
import io.snice.time.Clock;
import io.snice.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class NettyListeningPoint<T> implements ListeningPoint<T> {

    protected static final Logger logger = LoggerFactory.getLogger(NettyListeningPoint.class);

    private final URI listenAddress;
    private final Optional<URI> vipAddress;
    private final Transport transport;
    private final InetSocketAddress localAddress;
    private final int localPort;

    protected final Clock clock;

    /**
     * @param transport
     * @param listenAddress
     * @param vipAddress
     */
    private NettyListeningPoint(final Transport transport,
                                final URI listenAddress,
                                final URI vipAddress,
                                final Clock clock) {
        this.listenAddress = listenAddress;
        this.vipAddress = Optional.ofNullable(vipAddress);
        this.transport = transport;
        this.localPort = NettyNetworkInterface.getPort(listenAddress.getPort(), transport);
        this.localAddress = new InetSocketAddress(listenAddress.getHost(), this.localPort);
        this.clock = clock;
    }

    @Override
    public String toString() {
        return toStringRepresentation();
    }

    @Override
    public final int getLocalPort() {
        return localPort;
    }

    @Override
    public final InetSocketAddress getLocalAddress() {
        return this.localAddress;
    }

    @Override
    public final String getLocalIp() {
        return this.localAddress.getHostString();
    }

    @Override
    public final Transport getTransport() {
        return this.transport;
    }

    @Override
    public final URI getListenAddress() {
        return this.listenAddress;
    }

    @Override
    public final Optional<URI> getVipAddress() {
        return this.vipAddress;
    }

    static Builder withListenAddress(final URI address) {
        assertNotNull(address, "The listen address cannot be null");
        return new Builder(address);
    }

    /**
     * Listening point for UDP
     */
    private static class NettyUdpListeningPoint<T> extends NettyListeningPoint<T> {

        private final Bootstrap bootstrap;

        /**
         * For UDP we will use this channel for all Connections we create
         * since we won't actually connect for real.
         */
        private final AtomicReference<Channel> udpChannel = new AtomicReference<>();

        private NettyUdpListeningPoint(final URI listenAddress,
                                       final URI vipAddress,
                                       final Bootstrap bootstrap,
                                       final Clock clock) {
            super(Transport.udp, listenAddress, vipAddress, clock);
            this.bootstrap = bootstrap;
        }

        private void setChannel(final Channel channel) {
            this.udpChannel.set(channel);
        }

        @Override
        public CompletableFuture<Void> up() {
            final CompletableFuture<Void> future = new CompletableFuture<>();

            final ChannelFuture channelFuture = this.bootstrap.bind(getLocalAddress());
            channelFuture.addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        setChannel(channelFuture.channel());
                        NettyListeningPoint.logger.info("Successfully bound to listening point: " + getListenAddress());
                        future.complete(null);
                    } else {
                        NettyListeningPoint.logger.info("Unable to bind to listening point: " + getListenAddress());
                        future.completeExceptionally(channelFuture.cause());
                    }
                }
            });
            return future;
        }

        @Override
        public CompletableFuture<Void> down() {
            final var closeFuture = new CompletableFuture<Void>();
            udpChannel.get().close().addListener(f -> {
                closeFuture.complete(null);
            });
            return closeFuture;
        }

        @Override
        public CompletableFuture<Connection<T>> connect(final InetSocketAddress remoteAddress) {
            final var f = new CompletableFuture();
            internalConnect(f, remoteAddress);
            return f;
        }

        @Override
        public Connection<T> connectDirect(final InetSocketAddress remoteAddress) {
            return internalConnect(new CompletableFuture<>(), remoteAddress);
        }

        private Connection<T> internalConnect(final CompletableFuture<Connection<T>> future, final InetSocketAddress remoteAddress) {
            // Since we don't actually connect when using UDP we will be firing off
            // a success event right away and then we have to rely on the NettyUdpInboundAdapter
            // to do the right thing. It will also have to complete the future we created
            // above.
            final Channel channel = udpChannel.get();
            final ChannelHandlerContext ctx = channel.pipeline().firstContext();
            final Connection<T> connection = new UdpConnection(channel, remoteAddress, getVipAddress());
            final Long arrivalTime = clock.getCurrentTimeMillis();

            final var evt = ConnectionAttempt.success(future, connection, arrivalTime);
            ctx.pipeline().firstContext().fireUserEventTriggered(evt);
            return connection;
        }
    }

    /**
     * Listening point for TCP
     */
    private static class NettyTcpListeningPoint<T> extends NettyListeningPoint<T> {

        private final Bootstrap bootstrap;
        private final ServerBootstrap serverBootstrap;

        /**
         * @param listenAddress
         * @param vipAddress
         * @param bootstrap
         * @param serverBootstrap
         * @param clock
         */
        private NettyTcpListeningPoint(final URI listenAddress,
                                       final URI vipAddress,
                                       final Bootstrap bootstrap,
                                       final ServerBootstrap serverBootstrap,
                                       final Clock clock) {
            super(Transport.tcp, listenAddress, vipAddress, clock);
            this.bootstrap = bootstrap;
            this.serverBootstrap = serverBootstrap;
        }

        @Override
        public CompletableFuture<Void> up() {
            final CompletableFuture<Void> future = new CompletableFuture<>();

            final ChannelFuture channelFuture = this.serverBootstrap.bind(getLocalAddress());
            channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
                if (channelFuture1.isDone() && channelFuture1.isSuccess()) {
                    NettyListeningPoint.logger.info("Successfully bound to listening point: " + getListenAddress());
                    future.complete(null);
                } else if (channelFuture1.isDone() && !channelFuture1.isSuccess()) {
                    NettyListeningPoint.logger.info("Unable to bind to listening point: " + getListenAddress());
                    future.completeExceptionally(channelFuture1.cause());
                }
            });
            return future;
        }

        @Override
        public CompletableFuture<Void> down() {
            return null;
        }

        @Override
        public CompletableFuture<Connection<T>> connect(final InetSocketAddress remoteAddress) {
            final CompletableFuture<Connection<T>> f = new CompletableFuture<>();
            final ChannelFuture channelFuture = bootstrap.connect(remoteAddress);

            // TODO: I believe will end up being all executed in the IO thread, including the f.complete(c) which
            // TODO: then is all the way up into the application. Will have to re-work that...
            // TODO: yep, verified! The application will now be executing code on this IO thread, which is
            // TODO: NOT NOT NOT good. Need to change this... Should probably try and get this
            // TODO: connect future to be completed once an event has been triggered instead.
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    final Channel channel = channelFuture.channel();
                    final Connection<T> c = new TcpConnection(channel, remoteAddress);
                    final var evt = ConnectionAttempt.success(f, c, clock.getCurrentTimeMillis());
                    channel.pipeline().firstContext().fireUserEventTriggered(evt);
                } else {
                    // TODO: since the channel wasn't established, we need to complete
                    // this future elseewhere... Needs to be posted to some other thread pool
                    final var remote = ConnectionEndpointId.create(Transport.tcp, remoteAddress);
                    final var evt = ConnectionAttempt.failure(f, remote, future.cause(), clock.getCurrentTimeMillis());
                    // f.completeExceptionally(future.cause());
                    logger.warn("Unable to establish the SCTP association", future.cause());
                }
            });
            return f;
        }
    }

    /**
     * Listening point for SCTP
     */
    private static class NettySctpListeningPoint<T> extends NettyListeningPoint<T> {

        private final Bootstrap bootstrap;
        private final ServerBootstrap serverBootstrap;

        /**
         * @param listenAddress
         * @param vipAddress
         * @param bootstrap
         * @param serverBootstrap
         * @param clock
         */
        private NettySctpListeningPoint(final URI listenAddress,
                                        final URI vipAddress,
                                        final Bootstrap bootstrap,
                                        final ServerBootstrap serverBootstrap,
                                        final Clock clock) {
            super(Transport.sctp, listenAddress, vipAddress, clock);
            this.bootstrap = bootstrap;
            this.serverBootstrap = serverBootstrap;
        }

        @Override
        public CompletableFuture<Void> up() {
            final CompletableFuture<Void> future = new CompletableFuture<>();

            final ChannelFuture channelFuture = this.serverBootstrap.bind(getLocalAddress());
            channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
                if (channelFuture1.isDone() && channelFuture1.isSuccess()) {
                    NettyListeningPoint.logger.info("Successfully bound to listening point: " + getListenAddress());
                    future.complete(null);
                } else if (channelFuture1.isDone() && !channelFuture1.isSuccess()) {
                    NettyListeningPoint.logger.info("Unable to bind to listening point: " + getListenAddress());
                    future.completeExceptionally(channelFuture1.cause());
                }
            });
            return future;
        }

        @Override
        public CompletableFuture<Void> down() {
            return null;
        }

        @Override
        public CompletableFuture<Connection<T>> connect(final InetSocketAddress remoteAddress) {
            final CompletableFuture<Connection<T>> f = new CompletableFuture<>();

            try {
                // If we do not bind, then it will discover and use all available network interfaces
                // on your machine and include that in the INIT. If one of them then isn't routable
                // from the remote host, that remote host may send the HEARTBEAT to the wrong location
                // which will fail and then the entire connection dies. So, we have to do this...
                final ChannelFuture bindFuture = bootstrap.bind(new InetSocketAddress(getListenAddress().getHost(), 0));
                final ChannelFuture channelFuture = bindFuture.sync().channel().connect(remoteAddress);

                // TODO: I believe will end up being all executed in the IO thread, including the f.complete(c) which
                // TODO: then is all the way up into the application. Will have to re-work that...
                // TODO: yep, verified! The application will now be executing code on this IO thread, which is
                // TODO: NOT NOT NOT good. Need to change this... Should probably try and get this
                // TODO: connect future to be completed once an event has been triggered instead.
                channelFuture.addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        final Channel channel = channelFuture.channel();
                        final Connection<T> c = new SctpConnection(channel, remoteAddress);
                        final var evt = ConnectionAttempt.success(f, c, clock.getCurrentTimeMillis());
                        channel.pipeline().firstContext().fireUserEventTriggered(evt);
                    } else {
                        // TODO: since the channel wasn't established, we need to complete
                        // this future elseewhere... Needs to be posted to some other thread pool
                        final var remote = ConnectionEndpointId.create(Transport.tcp, remoteAddress);
                        final var evt = ConnectionAttempt.failure(f, remote, future.cause(), clock.getCurrentTimeMillis());
                        // f.completeExceptionally(future.cause());
                    }
                });
                return f;
            } catch (final Throwable t) {
                t.printStackTrace();
            }
            return null;
        }
    }

    static class Builder {
        private final URI listenAddress;
        private Transport transport;
        private URI vipAddress;

        private Bootstrap udpBootstrap;

        private Bootstrap tcpBootstrap;

        private Bootstrap sctpBootstrap;

        private ServerBootstrap tcpServerBootstrap;

        private ServerBootstrap sctpServerBootstrap;

        private Clock clock;

        private Builder(final URI address) {
            this.listenAddress = address;
        }

        public Builder withClock(final Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder withVipAddress(final URI vipAddress) {
            this.vipAddress = vipAddress;
            return this;
        }

        public Builder withTransport(final Transport transport) {
            this.transport = transport;
            return this;
        }

        public Builder withTransport(final URI vipAddress) {
            this.vipAddress = vipAddress;
            return this;
        }

        public Builder withUdpBootstrap(final Bootstrap bootstrap) {
            this.udpBootstrap = bootstrap;
            return this;
        }

        public Builder withSctpBootstrap(final Bootstrap bootstrap) {
            this.sctpBootstrap = bootstrap;
            return this;
        }

        public Builder withSctpServerBootstrap(final ServerBootstrap bootstrap) {
            this.sctpServerBootstrap = bootstrap;
            return this;
        }

        public Builder withTcpBootstrap(final Bootstrap bootstrap) {
            this.tcpBootstrap = bootstrap;
            return this;
        }

        public Builder withTcpServerBootstrap(final ServerBootstrap bootstrap) {
            this.tcpServerBootstrap = bootstrap;
            return this;
        }

        public NettyListeningPoint build() {
            assertNotNull(transport, "You must specify a transport");
            final Clock clock = this.clock != null ? this.clock : new SystemClock();
            if (transport.isTCP()) {
                assertNotNull(tcpBootstrap, "You must specify the TCP bootstrap");
                assertNotNull(tcpServerBootstrap, "You must specify the TCP server bootstrap");
                return new NettyTcpListeningPoint(listenAddress, vipAddress, tcpBootstrap, tcpServerBootstrap, clock);
            } else if (transport.isUDP()) {
                assertNotNull(udpBootstrap, "You must specify the UDP bootstrap");
                return new NettyUdpListeningPoint(listenAddress, vipAddress, udpBootstrap, clock);
            } else if (transport.isSCTP()) {
                assertNotNull(sctpBootstrap, "You must specify the SCTP bootstrap");
                assertNotNull(sctpServerBootstrap, "You must specify the SCTP server bootstrap");
                return new NettySctpListeningPoint(listenAddress, vipAddress, sctpBootstrap, sctpServerBootstrap, clock);
            }

            throw new IllegalTransportException("Currently we only support UDP and TCP");
        }
    }

}
