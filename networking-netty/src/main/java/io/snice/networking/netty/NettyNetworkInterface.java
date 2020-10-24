package io.snice.networking.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.snice.networking.common.Connection;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;
import io.snice.networking.config.NetworkInterfaceConfiguration;
import io.snice.networking.core.ListeningPoint;
import io.snice.networking.core.NetworkInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import static io.snice.preconditions.PreConditions.assertNotNull;
import static io.snice.preconditions.PreConditions.ensureNotNull;

/**
 * @author jonas@jonasborjesson.com
 */
public final class NettyNetworkInterface<T> implements NetworkInterface<T>, ChannelFutureListener {

    private final Logger logger = LoggerFactory.getLogger(NettyNetworkInterface.class);

    private final String name;

    private CountDownLatch latch;
    private final Bootstrap udpBootstrap;

    private final Bootstrap tcpBootstrap;

    private final ServerBootstrap tcpServerBootstrap;

    private final Bootstrap sctpBootstrap;

    private final ServerBootstrap sctpServerBootstrap;

    private final List<ListeningPoint> listeningPoints;

    private final ListeningPoint[] listeningPointsByTransport = new ListeningPoint[Transport.values().length];


    private NettyNetworkInterface(final String name, final Bootstrap udpBootstrap,
                                  final Bootstrap tcpBootstrap,
                                  final ServerBootstrap tcpServerBootstrap,
                                  final Bootstrap sctpBootstrap,
                                  final ServerBootstrap sctpServerBootstrap,
                                  final List<ListeningPoint> lps) {
        this.name = name;
        this.udpBootstrap = udpBootstrap;
        this.tcpBootstrap = tcpBootstrap;
        this.tcpServerBootstrap = tcpServerBootstrap;
        this.sctpBootstrap = sctpBootstrap;
        this.sctpServerBootstrap = sctpServerBootstrap;
        this.listeningPoints = lps;
        lps.forEach(lp -> listeningPointsByTransport[lp.getTransport().ordinal()] = lp);
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Bring this interface up, as in start listening to its dedicated listening points.
     */
    @Override
    public CompletionStage<Void> up() {
        final List<CompletableFuture<Void>> futures = new CopyOnWriteArrayList<>();
        this.listeningPoints.forEach(lp -> {
            futures.add(lp.up());
        });
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public CompletionStage<Void> down() {
        final List<CompletableFuture<Void>> futures = new CopyOnWriteArrayList<>();
        this.listeningPoints.forEach(lp -> {
            futures.add(lp.down());
        });
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public static int getPort(final int port, final Transport transport) {
        if (port >= 0) {
            return port;
        }

        // TODO: needs to change since this is geared towards SIP
        if (transport == Transport.tls) {
            return 5061;
        }

        if (transport == Transport.ws) {
            return 5062;
        }

        if (transport == Transport.sctp) {
            // TODO: not sure about this one but since
            // we currently do not support it then
            // let's leave it like this for now.
            return 5060;
        }

        return 5060;
    }

    @Override
    public ListeningPoint getListeningPoint(final Transport transport) {
        return listeningPointsByTransport[transport.ordinal()];
    }

    @Override
    public boolean isSupportingTransport(final Transport transport) {
        return listeningPointsByTransport[transport.ordinal()] != null;
    }

    /**
     * Use this {@link NettyNetworkInterface} to connect to a remote address using the supplied
     * {@link Transport}.
     * <p>
     * Note, if the {@link Transport} is a connection less transport, such as UDP, then there isn't
     * a "connect" per se.
     *
     * @param remoteAddress
     * @param transport
     * @return a {@link ChannelFuture} that, once completed, will contain the {@link Channel} that
     * is connected to the remote address.
     * @throws IllegalTransportException in case the {@link NettyNetworkInterface} isn't configured with
     *                                   the specified {@link Transport}
     */
    @Override
    public CompletionStage<Connection<T>> connect(final Transport transport, final InetSocketAddress remoteAddress)
            throws IllegalTransportException {
        final ListeningPoint lp = listeningPointsByTransport[transport.ordinal()];
        if (lp == null) {
            final String msg = String.format("Interface \"%s\" is not listening on transport %s", name, transport);
            throw new IllegalTransportException(msg);
        }

        return lp.connect(remoteAddress);


        // if (transport == Transport.udp || transport == null) {
        // final ListeningPoint lp2 = listeningPointsByTransport[Transport.udp.ordinal()];
        // return this.udpBootstrap.connect(remoteAddress, lp.getLocalAddress());
        // return null;

        // final UdpConnection connection = new UdpConnection(lp.getChannel(), remoteAddress);
        // final ChannelFuture future = lp.getChannel().newSucceededFuture();
        // return future;

            /*
            f.addListener(new GenericFutureListener<ChannelFuture>(){

                @Override
                public void operationComplete(final ChannelFuture future) throws Exception {
                    System.err.println("Future success " + future.isSuccess());
                    final Channel channel = future.channel();
                    System.err.println("Future completed so I guess I'm connected " + channel);
                    System.err.println("Remote Address: " + channel.remoteAddress());
                    System.err.println("Local Address: " + channel.localAddress());

                }
            });
            */

            /*
            final InetSocketAddress remote2 = new InetSocketAddress("192.168.0.100", 8576);
            final ChannelFuture f2 = lp.getChannel().connect(remote2);
            f2.addListener(new GenericFutureListener<ChannelFuture>(){

                @Override
                public void operationComplete(final ChannelFuture future) throws Exception {
                    System.err.println("Future2 success " + future.isSuccess());
                    System.err.println("Future2 cause " + future.cause());
                    final Channel channel = future.channel();
                    System.err.println("Future2 completed so I guess I'm connected " + channel);
                    System.err.println("Remote2 Address: " + channel.remoteAddress());
                    System.err.println("Local2 Address: " + channel.localAddress());

                }
            });
            */
        // final UdpConnection connection = new UdpConnection(lp.getChannel(), remoteAddress);
        // return this.udpBootstrap.group().next().newSucceededFuture(connection);
        // }

        // TODO: TCP
        // TODO: TLS
        // TODO: WS
        // TODO: WSS

        // throw new IllegalTransportException("Stack has not been configured for transport " + transport);
    }


    static Builder with(final NetworkInterfaceConfiguration config) {
        assertNotNull(config);
        return new Builder(config);
    }


    public static class Builder {
        private final NetworkInterfaceConfiguration config;

        /**
         * Our netty boostrap for connection less protocols
         */
        private Bootstrap udpBootstrap;

        private Bootstrap tcpBootstrap;

        private Bootstrap sctpBootstrap;

        private ServerBootstrap tcpServerBootstrap;

        private ServerBootstrap sctpServerBootstrap;

        private CountDownLatch latch;


        private Builder(final NetworkInterfaceConfiguration config) {
            this.config = config;
        }

        public Builder latch(final CountDownLatch latch) {
            this.latch = latch;
            return this;
        }

        public Builder udpBootstrap(final Bootstrap bootstrap) {
            this.udpBootstrap = bootstrap;
            return this;
        }

        public Builder tcpBootstrap(final Bootstrap bootstrap) {
            this.tcpBootstrap = bootstrap;
            return this;
        }

        public Builder sctpBootstrap(final Bootstrap bootstrap) {
            this.sctpBootstrap = bootstrap;
            return this;
        }

        public Builder sctpServerBootstrap(final ServerBootstrap bootstrap) {
            this.sctpServerBootstrap = bootstrap;
            return this;
        }

        public Builder tcpServerBootstrap(final ServerBootstrap bootstrap) {
            this.tcpServerBootstrap = bootstrap;
            return this;
        }

        public NettyNetworkInterface build() {
            ensureNotNull(this.latch, "Missing the latch");
            if (this.config.hasUDP()) {
                ensureNotNull(this.udpBootstrap, "You must configure a connectionless bootstrap");
            }

            if (this.config.hasTCP()) {
                ensureNotNull(this.tcpBootstrap, "You must configure a connection oriented bootstrap");
            }

            if (this.config.hasSCTP()) {
                ensureNotNull(this.sctpBootstrap, "You must configure the sctp oriented bootstrap");
            }

            if (this.config.hasTLS() || this.config.hasWS()) {
                throw new IllegalTransportException("Sorry, can only do TCP and UDP for now");
            }

            final URI listenAddress = this.config.getListeningAddress();
            final URI vipAddress = this.config.getVipAddress();
            final List<ListeningPoint> lps = new ArrayList<>();
            this.config.getTransports().forEach(t -> {
                // final URI listen = SipURI.withTemplate(listenAddress).withTransport(t).build();
                final NettyListeningPoint lp = NettyListeningPoint.withListenAddress(listenAddress)
                        .withTransport(t)
                        .withVipAddress(vipAddress)
                        .withTcpBootstrap(tcpBootstrap)
                        .withTcpServerBootstrap(tcpServerBootstrap)
                        .withUdpBootstrap(udpBootstrap)
                        .withSctpBootstrap(sctpBootstrap)
                        .withSctpServerBootstrap(sctpServerBootstrap)
                        .build();
                lps.add(lp);
            });

            return new NettyNetworkInterface(this.config.getName(),
                    this.udpBootstrap,
                    this.tcpBootstrap,
                    this.tcpServerBootstrap,
                    this.sctpBootstrap,
                    this.sctpServerBootstrap,
                    Collections.unmodifiableList(lps));
        }

    }


    @Override
    public void operationComplete(final ChannelFuture future) throws Exception {
        // TODO Auto-generated method stub

    }

}
