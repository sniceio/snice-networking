package io.snice.networking.app.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;
import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.common.event.ConnectionActiveIOEvent;
import io.snice.networking.common.event.ConnectionAttemptCompletedIOEvent;
import io.snice.networking.common.event.ConnectionIOEvent;
import io.snice.networking.common.event.MessageIOEvent;
import io.snice.networking.core.event.ConnectionAttemptSuccess;
import io.snice.networking.core.event.NetworkEvent;
import io.snice.networking.netty.UdpConnection;
import io.snice.time.Clock;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.*;
import java.util.function.BiFunction;

/**
 * Adapter to handle all incoming UDP traffic for a particular channel. Since
 * UDP isn't a connection oriented protocol, but we are presenting everything as
 * such, we will create {@link UdpConnection}s when we receive messages
 * from remote peers so in some sense, this {@link UdpConnection} represents
 * a "flow of udp packets between two ip:port pairs".
 *
 * NOTE: this class is not sharable. If you do not know what that means, read up on
 * the Netty ChannelHandlers and @Sharable (the netty annotation). In short, each
 * channel will have it's own handler because it stores states that is unique
 * to only that channel and as such, it cannot be shared...
 */
public class NettyUdpInboundAdapter<T> extends ChannelDuplexHandler {

    private final Clock clock;
    private final Optional<URI> vipAddress;
    private final UUID uuid = UUID.randomUUID();
    private final List<ConnectionContext> ctxs;

    private final Map<ConnectionId, ConnectionAdapter<UdpConnection<T>, T>> adapters;

    private final ConnectionContext defaultCtx;

    private InetSocketAddress localAddress;

    public NettyUdpInboundAdapter(final Clock clock , final Optional<URI> vipAddress, final List<ConnectionContext> ctxs) {
        this.clock = clock;
        this.vipAddress = vipAddress;
        this.ctxs = ctxs;

        adapters = allocateInitialMapSize();

        // TODO
        defaultCtx = null;
    }

    @Override
    public void connect(final ChannelHandlerContext ctx, final SocketAddress remoteAddress,
                        final SocketAddress localAddress, final ChannelPromise promise) throws Exception {
        log("Connecting...");
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void bind(final ChannelHandlerContext ctx, final SocketAddress localAddress,
                     final ChannelPromise promise) throws Exception {
        log("Binding to: " + localAddress);
        ctx.bind(localAddress, promise);
    }

    /**
     * We really do not want to re-hash the connection map so it is important
     * that you can configure the initial size of this map from the beginning.
     *
     * Also, unlike e.g. TCP, the UDP connections do not have a "natural" end so they
     * will have to be purged somehow... perhaps I shouldn't store them and just
     * create them everytime. Wonder what is more expensive...
     *
     * TODO: pass in some configuration object...
     */
    private Map<ConnectionId, ConnectionAdapter<UdpConnection<T>, T>> allocateInitialMapSize() {
        return new HashMap<>();
    }

    private void log(final String msg) {
        System.out.println("[ " + uuid + " UDP ]: " + msg);
    }

    private void logError(final String msg) {
        System.err.println("[ " + uuid + " TCP ]: " + msg);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final var udp = (UdpReadEvent<T>) msg;
        final var remote = udp.getRaw().sender();

        // TODO: if this is an IPv6 address, it'll blow up...
        final var id = ConnectionId.create(Transport.udp, udp.getRaw().recipient(), remote);

        // TODO: store away the connection instead so we don't have to find the
        // context on every incoming packet! See SNICE-28
        final var connCtx = findContext(id);
        if (connCtx.isDrop()) {
            ctx.close();
            return;
        }

        try {
            final var connection = new UdpConnection<T>(ctx.channel(), remote, vipAddress);
            final var channelContext = new DefaultChannelContext<T>(connection, connCtx);
            final var evt = MessageIOEvent.create(channelContext, clock.getCurrentTimeMillis(), udp.getMessage());
            ctx.fireChannelRead(evt);
        } catch (final ClassCastException e) {
            // TODO: this means that the underlying decoder isn't doing it's job...
            e.printStackTrace();
        }
    }

    private static Buffer toBuffer(final DatagramPacket pkt) {
        final ByteBuf content = pkt.content();
        final byte[] b = new byte[content.readableBytes()];
        content.getBytes(0, b);
        return Buffers.wrap(b);
    }

    private ConnectionContext<Connection<T>, T> findContext(final ConnectionId id) {
        return ctxs.stream().filter(ctx -> ctx.test(id)).findFirst().orElse(defaultCtx);
    }

    private ConnectionAdapter<UdpConnection<T>, T> ensureConnection(final Channel channel, final ConnectionId id, final ConnectionContext<Connection<T>, T> connCtx) {
        return adapters.computeIfAbsent(id, cId -> new ConnectionAdapter(new UdpConnection(channel, id, vipAddress), connCtx));
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        ctx.fireChannelReadComplete();
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        log("UserEventTriggered: " + evt);
        try {
            final NetworkEvent<T> event = (NetworkEvent<T>) evt;
            if (event.isConnectionAttemptEvent()) {
                processConnectionAttemptCompleted(ctx, event.toConnectionAttemptSuccess());
            }

        } catch (final ClassCastException e) {
            // TODO: need to introduce proper logging and this should
            // be a WARN with an AlertCode.
            logError("Unknown user event triggered. Dropping event " + evt.getClass().getName() + " toString: " + evt);
        }
    }

    /**
     * Unlike SCTP and TCP, there is no "active" event for UDP but in order to maintain the same
     * irrespective of transport, we'll first be firing off the active event and then
     * the complete event.
     * <p>
     * Inbound v.s. outbound connection:</br>
     * Also note that if we have a waiting future on the connection event, it was the user who initiated this
     * "connection", which would make this an outbound connection. If there is no future, then
     * this is an inbound connection.
     *
     * @param ctx
     * @param evt
     */
    private void processConnectionAttemptCompleted(final ChannelHandlerContext ctx, final ConnectionAttemptSuccess<T> evt) {
        if (evt == null) {
            return;
        }

        // TODO: if someone connects to the same local:ip/remote:ip port pair, we should
        // TODO: store that away so we don't do this every time...
        // Also, if we did that then we would also be able to keep track of inbound "connections"
        final var connection = evt.getConnection();
        final var connectionContext = findContext(connection.id());
        final var channelContext = new DefaultChannelContext<T>(connection, connectionContext);

        final var isInboundConnection = evt.getUserConnectionFuture() == null;
        final var connectionActiveIOEvent = ConnectionActiveIOEvent.create(channelContext, isInboundConnection, evt.getArrivalTime());
        ctx.fireUserEventTriggered(connectionActiveIOEvent);

        final var e = ConnectionAttemptCompletedIOEvent.create(channelContext, evt.getUserConnectionFuture(), connection, evt.getArrivalTime());
        ctx.fireUserEventTriggered(e);
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        log("Exception");
        cause.printStackTrace();
    }

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        log("Channel Registered");
    }

    private ConnectionIOEvent create(final ChannelHandlerContext ctx, final BiFunction<Connection, Long, ConnectionIOEvent> f) {
        final Channel channel = ctx.channel();
        System.out.println("UDP Adapter: create " + Thread.currentThread().getName() + " Local: " + channel.localAddress() + " Remote: " + channel.remoteAddress());
        final Connection connection = new UdpConnection(channel, (InetSocketAddress) channel.remoteAddress(), vipAddress);
        final Long arrivalTime = clock.getCurrentTimeMillis();
        return f.apply(connection, arrivalTime);
    }
    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        // TODO: the FlowActor should transition to the CLOSED state.
        // System.err.println("UDP Decoder: Channel un-registered " + ctx.channel());
    }
    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        log("Channel active " + ctx.channel());
        localAddress = (InetSocketAddress)ctx.channel().localAddress();
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        // TODO: this would be the closing event
        // TODO:
        log("Channel in-active " + ctx.channel());
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext ctx) throws Exception {
        System.err.println("UDP Decoder: Channel writability changed");
        // ctx.fireChannelWritabilityChanged();
    }
}
