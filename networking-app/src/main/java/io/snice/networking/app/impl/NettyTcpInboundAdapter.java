package io.snice.networking.app.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.netty.TcpConnection;
import io.snice.time.Clock;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * NOTE: this class is not sharable. If you do not know what that means, read up on
 * the Netty ChannelHandlers and @Sharable (the netty annotation). In short, each
 * channel will have it's own handler because it stores states that is unique
 * to only that channel and as such, it cannot be shared...
 *
 */
public class NettyTcpInboundAdapter<T> implements ChannelInboundHandler {

    private final Clock clock;
    private final Optional<URI> vipAddress;
    private final UUID uuid = UUID.randomUUID();
    private final List<ConnectionContext> ctxs;

    /**
     * If the incoming connection doesn't match anything, then we'll use this
     * default context instead.
     */
    private final ConnectionContext defaultCtx;

    /**
     * Represents the underlying channel and for TCP, we will always have
     * a local and remote peer since this is after all a connection oriented
     * protocol.
     */
    private ConnectionAdapter<TcpConnection, T> connection;

    public NettyTcpInboundAdapter(final Clock clock , final Optional<URI> vipAddress, final List<ConnectionContext> ctxs) {
        this.clock = clock;
        this.vipAddress = vipAddress;
        this.ctxs = ctxs;

        // TODO
        defaultCtx = null;
    }

    private void log(final String msg) {
        System.out.println("[ " + uuid + " TCP ]: " + msg);
    }

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        /*
        log("channel registered");
        if (ctx.channel().localAddress() != null) {
            ctx.fireUserEventTriggered(create(ctx, ConnectionOpenedIOEvent::create));
        }
         */
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        /*
        log("channel unregistered");
        if (ctx.channel().localAddress() != null) {
            ctx.fireUserEventTriggered(create(ctx, ConnectionClosedIOEvent::create));
        }
         */
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        if (connection != null) {
            System.err.println("WTF - you can't have yet another connection coming in???");
        }

        final var channel = ctx.channel();
        final var localAddress = (InetSocketAddress)channel.localAddress();
        final var remoteAddress = (InetSocketAddress)channel.remoteAddress();
        final var id = ConnectionId.create(Transport.tcp, localAddress, remoteAddress);
        final var connCtx = findContext(id);

        if (connCtx.isDrop()) {
            ctx.close();
            return;
        }

        connection = new ConnectionAdapter(new TcpConnection(channel, id, vipAddress), null, connCtx);
    }

    private ConnectionContext<Connection, T> findContext(final ConnectionId id) {
        return ctxs.stream().filter(ctx -> ctx.test(id)).findFirst().orElse(defaultCtx);
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        log("channel inactive");
        if (ctx.channel().localAddress() != null) {
            // final ConnectionIOEvent event = create(ctx, ConnectionInactiveIOEvent::create);
            // ctx.fireUserEventTriggered(event);
        }
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final var buffer = (ByteBuf)msg;
        while (buffer.isReadable()) {
            final int availableBytes = buffer.readableBytes();
            final int toWrite = Math.min(availableBytes, availableBytes);
            final byte[] data = new byte[toWrite];
            buffer.readBytes(data);
            connection.frame(Buffers.wrap(data)).ifPresent(connection::process);
        }
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext ctx) throws Exception {
        // just consume the event
        log("writability changed");
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        log("handler added " + ctx.name());

    }

    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
        log("handler removed : " + ctx.name());

    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        log("exception " + cause.getMessage());
        cause.printStackTrace();
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        log("user event");
    }

}
