package io.snice.networking.app.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;
import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.common.event.ConnectionActiveIOEvent;
import io.snice.networking.common.event.ConnectionAttemptCompletedIOEvent;
import io.snice.networking.common.event.MessageIOEvent;
import io.snice.networking.core.event.ConnectionAttemptSuccess;
import io.snice.networking.core.event.NetworkEvent;
import io.snice.networking.netty.UdpConnection;
import io.snice.time.Clock;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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

    private final Map<ConnectionId, ChannelContext<T>> channels;

    private final ConnectionContext defaultCtx;

    private InetSocketAddress localAddress;

    public NettyUdpInboundAdapter(final Clock clock , final Optional<URI> vipAddress, final List<ConnectionContext> ctxs) {
        this.clock = clock;
        this.vipAddress = vipAddress;
        this.ctxs = ctxs;

        channels = allocateInitialMapSize();

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
     * <p>
     * Also, unlike e.g. TCP, the UDP connections do not have a "natural" end so they
     * will have to be purged somehow... perhaps I shouldn't store them and just
     * create them everytime. Wonder what is more expensive...
     * <p>
     * TODO: pass in some configuration object...
     */
    private Map<ConnectionId, ChannelContext<T>> allocateInitialMapSize() {
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


        try {

            final var id = ConnectionId.create(Transport.udp, udp.getRaw().recipient(), remote);
            final var channelContext = ensureContext(ctx, id, null, udp.getArrivalTime());

            // null context if e.g. the user has decided to drop the connection.
            // TODO: if the user has a function to run on the drop, it isn't being executed right tnow...
            // TODO: should return an object instead so it is more obvious
            if (channelContext == null) {
                return;
            }

            final var evt = MessageIOEvent.create(channelContext, clock.getCurrentTimeMillis(), udp.getMessage());
            ctx.fireChannelRead(evt);
        } catch (final ArrayIndexOutOfBoundsException e) {
            // TODO: because the ConnectionId.create assumes it's an IPv4 address.
            throw new RuntimeException("Sorry, currently the internal ConnectionId assumes everything is IPv4 and as such " +
                    "this isn't working. Please restart your application with the JVM argument " +
                    "\"-Djava.net.preferIPv4Stack=true\" and it'll be fine");
        } catch (final ClassCastException e) {
            // TODO: this means that the underlying decoder isn't doing it's job...
            e.printStackTrace();
        }
    }

    /**
     * A "connection" can be created in two ways. Either based on incoming traffic or, based on a user
     * requesting to create an "outbound" connection. Either or, they are both representing the exact same
     * connection and as such, we need to ensure we do not end up creating the same connection twice just because
     * a user, and incoming traffic, happened to come in at once (and from the same remote ip:port).
     *
     * @param ctx
     * @param id
     * @param connectionFuture
     * @param arrivalTime
     * @return
     */
    private ChannelContext<T> ensureContext(final ChannelHandlerContext ctx,
                                            final ConnectionId id,
                                            final CompletableFuture<Connection<T>> connectionFuture,
                                            final long arrivalTime) {
        final boolean isInbound = connectionFuture == null;
        final var udpConnection = new UdpConnection(ctx.channel(), id, vipAddress);

        final var cCtx = channels.computeIfAbsent(id, cId -> {

            System.err.println("Compute if absent " + id);

            // if we do not have a connection future then this connection was NOT initiated by
            // the application and as such, this is an "inbound" connection, as opposed to an "outbound"

            final var connCtx = findContext(id);
            // only drop if this is an inbound connection attempt. For outbound, the user
            // obviously requested it so we need to let it through.
            if (isInbound && connCtx.isDrop()) {
                ctx.close();
                // Dead adapter so we stop other attempts?
                return null;
            }

            try {

                return new DefaultChannelContext<T>(udpConnection, connCtx);
            } catch (final Throwable th) {
                th.printStackTrace();
                throw th;
            }
        });

        // TODO: need to re-work.
        // These events are being fired to maintain the same contract as with
        // connection oriented protocols, such as TCP and SCTP. However, the below
        // approach is "dangerous" in that it takes place within the lock of
        // the concurrent hash map and as such, if the user holds onto anything
        // it has the potential of blocking network traffic.
        //

        final var connectionActiveIOEvent = ConnectionActiveIOEvent.create(cCtx, isInbound, arrivalTime);
        ctx.fireUserEventTriggered(connectionActiveIOEvent);

        // Note that for this inbound UDP "connection", there is no future waiting to be completed since
        // this is not a connection the user initiated. However, the user may have specified a
        // "save" function, which will be called by the NettyApplicationLayer in order to invoke the app.
        final var e = ConnectionAttemptCompletedIOEvent.create(cCtx, connectionFuture, udpConnection, arrivalTime);
        ctx.fireUserEventTriggered(e);
        return cCtx;
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
        final var id = connection.id();
        ensureContext(ctx, id, evt.getUserConnectionFuture(), evt.getArrivalTime());
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
