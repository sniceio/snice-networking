/**
 * 
 */
package io.snice.networking.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.SctpChannelOption;
import io.netty.channel.sctp.nio.NioSctpChannel;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;
import io.snice.networking.config.NetworkInterfaceConfiguration;
import io.snice.networking.core.ListeningPoint;
import io.snice.networking.core.NetworkInterface;
import io.snice.networking.core.NetworkLayer;
import io.snice.time.Clock;
import io.snice.time.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.snice.preconditions.PreConditions.assertNotNull;
import static io.snice.preconditions.PreConditions.ensureNotEmpty;
import static io.snice.preconditions.PreConditions.ensureNotNull;

/**
 * The {@link NettyNetworkLayer} is the glue between the network (netty) and
 * the rest of the SIP stack and eventually the users own io.sipstack.application.application.
 * 
 * The purpose of this {@link NettyNetworkLayer} is simply to read/write from/to
 * the channels and then dispatch the result of those operations to
 * the actual SIP Stack.
 * 
 * @author jonas@jonasborjesson.com
 */
public class NettyNetworkLayer<T> implements NetworkLayer<T> {

    private static final Logger logger = LoggerFactory.getLogger(NettyNetworkLayer.class);

    private final CompletableFuture<Void> shutdownStage = new CompletableFuture<>();

    /**
     * Every {@link ListeningPoint} has a reference to the very same {@link CountDownLatch}
     * and each of those {@link ListeningPoint}s will call {@link CountDownLatch#countDown()}
     * when they are finished shutting down the socket again. Hence, we can use this
     * to hang on the latch until everything is shut down again.
     */
    private final CountDownLatch latch;

    private final ConcurrentMap<String, NettyNetworkInterface<T>> interfaces;

    private final NettyNetworkInterface defaultInterface;

    private final Bootstraps bootstraps;

    /**
     *
     */
    private NettyNetworkLayer(final CountDownLatch latch, final Bootstraps bootstraps, final List<NettyNetworkInterface<T>> ifs) {
        this.latch = latch;
        this.interfaces = new ConcurrentHashMap<>();
        this.bootstraps = bootstraps;

        ifs.forEach(nic -> {
            interfaces.put(nic.getName().toLowerCase(), nic);
        });

        // TODO: make this configurable. For now, it is simply the
        // first one...
        this.defaultInterface = ifs.get(0);
    }

    @Override
    public void start() {
        final List<CompletionStage<Void>> futures = new CopyOnWriteArrayList<>();
        this.interfaces.values().forEach(i -> futures.add(i.up()));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    @Override
    public void stop() {
        try {
            this.interfaces.values().forEach(NetworkInterface::down);
            latch.await();
            shutdownStage.complete(null);
        } catch (final InterruptedException e) {
            shutdownStage.completeExceptionally(e);
        }
    }

    @Override
    public CompletionStage<Connection<T>> connect(final Transport transport, final InetSocketAddress address) {
        return defaultInterface.connect(transport, address);
    }

    @Override
    public CompletionStage<Connection<T>> connect(final Transport transport, final int localPort, final InetSocketAddress address) {
        return connect("testing", transport, localPort, address);
    }

    // Copied from Environment.connect
    //
    // TODO: perhaps we should split these two "connect" methods into two separate steps so it is more
    // obvious that a new NIC is created.
    // e.g. addNewNetworkInterface
    //
    @Override
    public CompletionStage<Connection<T>> connect(final String name, final Transport transport, final int localPort, final InetSocketAddress address) {
        // System.err.println("Trying to connect over " + transport + " from local port: " + localPort + " to " + address);
        if (localPort == 0) {
            // TODO: not sure it should go here...
            try {
                final var listen = new URI(transport + "://0.0.0.0:0");
                final var nic = configureNewNetworkInterface(name, transport, listen);
                if (interfaces.putIfAbsent(nic.getName().toLowerCase(), nic) != null) {
                    throw new IllegalArgumentException("A NIC with the given name \"" + name + "\" already exists. These names must be unique");
                }

                nic.up().toCompletableFuture().get();
                // System.err.println("NIC should be up");
                return nic.connect(transport, address);
            } catch (final Throwable e) {
                e.printStackTrace();
                throw new RuntimeException("oops", e);
            }
        }

        final var lp = defaultInterface.getListeningPoint(transport);
        if (lp != null && lp.getLocalPort() == localPort) {
            return connect(transport, address);
        }

        throw new RuntimeException("Not yet fully implemented");
    }

    private NettyNetworkInterface<T> configureNewNetworkInterface(final String name, final Transport transport, final URI listen) {
        final var config = new NetworkInterfaceConfiguration(name, listen, null, List.of(transport));
        final NettyNetworkInterface.Builder builder = NettyNetworkInterface.with(config);
        builder.latch(new CountDownLatch(1)); // this doesn't seem to be used anymore...
        builder.udpBootstrap(bootstraps.udpBootstrap);
        builder.tcpBootstrap(bootstraps.tcpBootstrap);
        builder.tcpServerBootstrap(bootstraps.tcpServerBootstrap);
        builder.sctpBootstrap(bootstraps.sctpBootstrap);
        builder.sctpServerBootstrap(bootstraps.sctpServerBootstrap);
        return builder.build();
    }

    @Override
    public CompletionStage<Void> sync() {
        return shutdownStage;
    }

    @Override
    public NetworkInterface<T> getDefaultNetworkInterface() {
        return this.defaultInterface;
    }

    @Override
    public Optional<? extends NetworkInterface<T>> getNetworkInterface(final String name) {
        return Optional.ofNullable(interfaces.get(name.toLowerCase()));
    }

    @Override
    public Optional<? extends NetworkInterface<T>> getNetworkInterface(final String interfaceName, final Transport transport) {
        return getNetworkInterface(interfaceName).filter(i -> i.isSupportingTransport(transport));
    }

    @Override
    public List<? extends NetworkInterface<T>> getNetworkInterfaces() {
        return interfaces.values().stream().collect(Collectors.toList());
    }

    @Override
    public List<? extends NetworkInterface<T>> getNetworkInterfaces(final Transport transport) {
        return interfaces.values().stream().filter(i -> i.isSupportingTransport(transport)).collect(Collectors.toList());
    }

    @Override
    public Optional<ListeningPoint> getListeningPoint(final Transport transport) {
        return Optional.ofNullable(defaultInterface.getListeningPoint(transport));
    }

    @Override
    public Optional<ListeningPoint> getListeningPoint(final String networkInterfaceName, final Transport transport) {
        return Optional.ofNullable(interfaces.get(networkInterfaceName).getListeningPoint(transport));
    }

    public static Builder with(final List<NetworkInterfaceConfiguration> ifs) throws IllegalArgumentException {
        return new Builder(ensureNotNull(ifs));
    }

    public static Builder with(final NetworkInterfaceConfiguration i) throws IllegalArgumentException {
        return new Builder(List.of(ensureNotNull(i)));
    }

    private static class Handler {
        private final Supplier<ChannelHandler> handler;
        private final String name;

        /**
         * The transports to which this handler is supposed to be installed.
         */
        private final List<Transport> transports;

        private Handler(final String name, final ChannelHandler handler, final Transport... transports) {
            this(name, () -> handler, transports);
        }

        private Handler(final String name, final ChannelHandler handler, final List<Transport> transports) {
            this(name, () -> handler, transports);
        }

        private Handler(final String name, final Supplier<ChannelHandler> handler, final List<Transport> transports) {
            this.name = name;
            this.handler = handler;
            this.transports = transports;
        }

        private Handler(final String name, final Supplier<ChannelHandler> handler, final Transport... transports) {
            this(name, handler, Arrays.asList(transports));
        }

        public String getName() {
            return name;
        }

        public ChannelHandler getHandler() {
            return handler.get();
        }

        public boolean hasTransport(final Transport transport) {
            return transports.contains(transport);
        }
    }

    public static class Builder {

        private final List<NetworkInterfaceConfiguration> ifs;

        private EventLoopGroup bossGroup;
        private EventLoopGroup workerGroup;
        private EventLoopGroup udpGroup;
        private EventLoopGroup sctpGroup;
        private Clock clock;

        /**
         * The TCP based bootstrap.
         */
        private ServerBootstrap serverBootstrap;

        /**
         * Our UDP based bootstrap.
         */
        private Bootstrap bootstrap;

        /**
         * Our SCTP based bootstrap.
         */
        private Bootstrap sctpBootstrap;

        /**
         * The SCTP based server bootstrap.
         */
        private ServerBootstrap sctpServerBootstrap;

        private final List<Handler> handlers = new ArrayList<>();

        private Builder(final List<NetworkInterfaceConfiguration> ifs) {
            this.ifs = ifs;
        }

        public Builder withHandler(final String handlerName, final ChannelHandler handler) {
            return withHandler(handlerName, handler, Transport.values());
        }

        public Builder withHandler(final String handlerName, final ChannelHandler handler, final Transport... transports) {
            ensureNotEmpty(handlerName, "The name of the handler cannot be null or the empty string");
            ensureNotNull(handler, "The handler cannot be null");
            ensureNotNull(transports, "You must specify at least one transport that this handler will be installed");
            handlers.add(new Handler(handlerName, handler, transports));
            return this;
        }

        public Builder withHandler(final List<ProtocolHandler> handlers) {
            assertNotNull(handlers);
            handlers.forEach(this::withHandler);
            return this;
        }

        public Builder withHandler(final ProtocolHandler handler) {
            assertNotNull(handler);
            withHandler(handler.getName(), handler.getDecoder(), handler.getTransports());
            return this;
        }

        public Builder withHandler(final String handlerName, final Supplier<ChannelHandler> handler, final Transport... transports) {
            ensureNotEmpty(handlerName, "The name of the handler cannot be null or the empty string");
            ensureNotNull(handler, "The handler cannot be null");
            ensureNotNull(transports, "You must specify at least one transport that this handler will be installed");
            handlers.add(new Handler(handlerName, handler, transports));
            return this;
        }

        public Builder withHandler(final String handlerName, final Supplier<ChannelHandler> handler, final List<Transport> transports) {
            ensureNotEmpty(handlerName, "The name of the handler cannot be null or the empty string");
            ensureNotNull(handler, "The handler cannot be null");
            ensureNotNull(transports, "You must specify at least one transport that this handler will be installed");
            handlers.add(new Handler(handlerName, handler, transports));
            return this;
        }

        public Builder withClock(final Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder withBossEventLoopGroup(final EventLoopGroup group) {
            this.bossGroup = group;
            return this;
        }

        public Builder withTCPEventLoopGroup(final EventLoopGroup group) {
            this.workerGroup = group;
            return this;
        }

        public Builder withUDPEventLoopGroup(final EventLoopGroup group) {
            this.udpGroup = group;
            return this;
        }

        public Builder withSCTPEventLoopGroup(final EventLoopGroup group) {
            this.sctpGroup = group;
            return this;
        }

        public NettyNetworkLayer build() {

            // TODO: check that if you e.g. specify dialog layer then you must also specify transaction layer

            // TOOD: need to re-work all of the default values for the various event loop groups...
            if (bossGroup == null) {
                bossGroup = new NioEventLoopGroup();
            }

            workerGroup = workerGroup == null ? new NioEventLoopGroup() : workerGroup;
            udpGroup = udpGroup == null ? workerGroup : udpGroup;
            sctpGroup = sctpGroup == null ? workerGroup : sctpGroup;

            final Clock clock = this.clock != null ? this.clock : new SystemClock();

            final var udpBootstrap = ensureUDPBootstrap();
            final var tcpBootstrap = ensureTCPBootstrap();
            final var sctpBootstrap = ensureSctpBootstrap();
            final var tcpServerBootstrap = ensureTCPServerBootstrap();
            final var sctpServerBootstrap = ensureSctpServerBootstrap();
            final var bootstraps = new Bootstraps(udpBootstrap, tcpBootstrap, sctpBootstrap, tcpServerBootstrap, sctpServerBootstrap);

            final List<NettyNetworkInterface.Builder> builders = new ArrayList<>();
            final var interfaces = ifs.isEmpty() ? createDefaultNetworkInterfaceListeningPoint() : ifs;
            interfaces.forEach(i -> {
                final NettyNetworkInterface.Builder ifBuilder = NettyNetworkInterface.with(i);
                ifBuilder.udpBootstrap(udpBootstrap);
                ifBuilder.tcpBootstrap(tcpBootstrap);
                ifBuilder.tcpServerBootstrap(tcpServerBootstrap);
                ifBuilder.sctpBootstrap(sctpBootstrap);
                ifBuilder.sctpServerBootstrap(sctpServerBootstrap);
                builders.add(ifBuilder);
            });

            final CountDownLatch latch = new CountDownLatch(builders.size());
            final List<NettyNetworkInterface> ifs = new ArrayList<>();
            builders.forEach(ifBuilder -> ifs.add(ifBuilder.latch(latch).build()));
            return new NettyNetworkLayer(latch, bootstraps, Collections.unmodifiableList(ifs));
        }

        private List<NetworkInterfaceConfiguration> createDefaultNetworkInterfaceListeningPoint() {
            try {
                final Inet4Address address = findPrimaryAddress();
                final String ip = address.getHostAddress();
                final String interfaceName = "default";
                final URI listen = new URI("whatever://" + ip + ":7777");
                final NetworkInterfaceConfiguration tcp
                        = new NetworkInterfaceConfiguration(interfaceName, listen, null, Transport.tcp);
                final NetworkInterfaceConfiguration udp
                        = new NetworkInterfaceConfiguration(interfaceName, listen, null, Transport.udp);
                return List.of(tcp, udp);
            } catch (final Exception e) {
                e.printStackTrace();
                throw new RuntimeException("unable to find suitable local interface");
            }
        }

        private Bootstrap ensureUDPBootstrap() {
            // TODO: this won't be correct when we listen to multiple ports
            // and they may have different vip addresses etc. we'll deal with that
            // later...
            if (this.bootstrap == null) {
                final Bootstrap b = new Bootstrap();
                b.group(this.udpGroup)
                        .channel(NioDatagramChannel.class)
                        .handler(new ChannelInitializer<DatagramChannel>() {
                            @Override
                            protected void initChannel(final DatagramChannel ch) throws Exception {
                                final ChannelPipeline pipeline = ch.pipeline();
                                handlers.stream()
                                        .filter(h -> h.hasTransport(Transport.udp))
                                        .forEach(h -> pipeline.addLast(h.getName(), h.getHandler()));
                            }
                        }).option(ChannelOption.SO_REUSEADDR, true);

                // this allows you to setup connections from the
                // same listening point
                // .option(ChannelOption.SO_REUSEADDR, true);

                this.bootstrap = b;
            }
            return this.bootstrap;
        }

        private Bootstrap ensureSctpBootstrap() {
            if (sctpBootstrap == null) {
                final Bootstrap b = new Bootstrap();
                b.group(sctpGroup)
                        .channel(NioSctpChannel.class)
                        .option(SctpChannelOption.SCTP_NODELAY, true)
                        .option(SctpChannelOption.SO_KEEPALIVE, true)
                        .handler(new ChannelInitializer<NioSctpChannel>() {
                            @Override
                            protected void initChannel(final NioSctpChannel ch) throws Exception {
                                final ChannelPipeline pipeline = ch.pipeline();
                                handlers.stream()
                                        .filter(h -> h.hasTransport(Transport.sctp))
                                        .forEach(h -> pipeline.addLast(h.getName(), h.getHandler()));
                            }
                        });

                sctpBootstrap = b;
            }

            return sctpBootstrap;
        }

        private ServerBootstrap ensureSctpServerBootstrap() {
            if (sctpServerBootstrap == null) {
                final ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, sctpGroup)
                        .channel(NioSctpServerChannel.class)
                        .option(SctpChannelOption.SO_KEEPALIVE, true)
                        .option(SctpChannelOption.SO_BACKLOG, 1000)
                        // .option(SctpChannelOption.SO_BACKLOG, 100)
                        .childHandler(new ChannelInitializer<NioSctpChannel>() {
                            @Override
                            public void initChannel(final NioSctpChannel ch) throws Exception {
                                final ChannelPipeline pipeline = ch.pipeline();
                                handlers.stream()
                                        .filter(h -> h.hasTransport(Transport.sctp))
                                        .forEach(h -> pipeline.addLast(h.getName(), h.getHandler()));
                            }
                        });

                sctpServerBootstrap = b;
            }

            return sctpServerBootstrap;
        }

        private Bootstrap ensureTCPBootstrap() {
            final Bootstrap tcpBootstrap = new Bootstrap();
            tcpBootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(final SocketChannel ch) throws Exception {
                            final ChannelPipeline pipeline = ch.pipeline();
                            handlers.stream()
                                    .filter(h -> h.hasTransport(Transport.tcp))
                                    .forEach(h -> pipeline.addLast(h.getName(), h.getHandler()));
                        }
                    });
            return tcpBootstrap;
        }

        private ServerBootstrap ensureTCPServerBootstrap() {
            if (this.serverBootstrap == null) {
                final ServerBootstrap b = new ServerBootstrap();

                b.group(this.bossGroup, this.workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(final SocketChannel ch) throws Exception {
                                final ChannelPipeline pipeline = ch.pipeline();
                                handlers.stream()
                                        .filter(h -> h.hasTransport(Transport.tcp))
                                        .forEach(h -> pipeline.addLast(h.getName(), h.getHandler()));
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.TCP_NODELAY, true);
                // TODO: should make all the above TCP options configurable
                this.serverBootstrap = b;
            }
            return this.serverBootstrap;
        }

        private static Inet4Address findPrimaryAddress() {
            java.net.NetworkInterface loopback = null;
            java.net.NetworkInterface primary = null;
            try {
                final Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    final java.net.NetworkInterface i = interfaces.nextElement();
                    if (i.isLoopback()) {
                        loopback = i;
                    } else if (i.isUp() && !i.isVirtual()) {
                        primary = i;
                        break;
                    }
                }
            } catch (final SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            final java.net.NetworkInterface network = primary != null ? primary : loopback;
            final Inet4Address address = getInet4Address(network);
            return address;
        }

        private static Inet4Address getInet4Address(final java.net.NetworkInterface network) {
            final Enumeration<InetAddress> addresses = network.getInetAddresses();
            while (addresses.hasMoreElements()) {
                final InetAddress address = addresses.nextElement();
                if (address instanceof Inet4Address) {
                    return (Inet4Address) address;
                }
            }
            return null;
        }
    }

    private static class Bootstraps {
        final Bootstrap udpBootstrap;
        final Bootstrap tcpBootstrap;
        final Bootstrap sctpBootstrap;
        final ServerBootstrap tcpServerBootstrap;
        final ServerBootstrap sctpServerBootstrap;

        private Bootstraps(
                final Bootstrap udpBootstrap,
                final Bootstrap tcpBootstrap,
                final Bootstrap sctpBootstrap,
                final ServerBootstrap tcpServerBootstrap,
                final ServerBootstrap sctpServerBootstrap) {
            this.udpBootstrap = udpBootstrap;
            this.tcpBootstrap = tcpBootstrap;
            this.sctpBootstrap = sctpBootstrap;
            this.tcpServerBootstrap = tcpServerBootstrap;
            this.sctpServerBootstrap = sctpServerBootstrap;
        }

    }

}
