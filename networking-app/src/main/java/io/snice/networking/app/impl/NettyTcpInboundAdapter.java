package io.snice.networking.app.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.codec.SerializationFactory;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.common.event.ConnectionActiveIOEvent;
import io.snice.networking.common.event.MessageIOEvent;
import io.snice.networking.netty.TcpConnection;
import io.snice.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static io.snice.preconditions.PreConditions.assertNull;

/**
 * NOTE: this class is not sharable. If you do not know what that means, read up on
 * the Netty ChannelHandlers and @Sharable (the netty annotation). In short, each
 * channel will have it's own handler because it stores states that is unique
 * to only that channel and as such, it cannot be shared...
 *
 */
public class NettyTcpInboundAdapter<T> implements ChannelInboundHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyTcpInboundAdapter.class);

    private final Clock clock;
    private final Optional<URI> vipAddress;

    // TODO: don't really need this one.
    private final UUID uuid = UUID.randomUUID();

    private final List<ConnectionContext> ctxs;
    private final SerializationFactory<T> factory;
    private final int exceptionCounter = 0;

    private final ReentrantLock lock = new ReentrantLock();


    /**
     * If the incoming connection doesn't match anything, then we'll use this
     * default context instead.
     */
    private final ConnectionContext defaultCtx;


    private TcpConnection<T> connection;
    private DefaultChannelContext<T> channelContext;

    public NettyTcpInboundAdapter(final Clock clock, final SerializationFactory<T> factory, final Optional<URI> vipAddress, final List<ConnectionContext> ctxs) {
        this.clock = clock;
        this.factory = factory;
        this.vipAddress = vipAddress;
        this.ctxs = ctxs;

        // TODO
        defaultCtx = null;
    }

    private void log(final String msg) {
        System.out.println("[ " + uuid + " TCP ]: " + msg);
    }

    private void logError(final String msg) {
        System.err.println("[ " + uuid + " TCP ]: " + msg);
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
        assertNull(connection, "There was already an active connection for this TCP Context, should be imposssible.");

        final var channel = ctx.channel();
        final var localAddress = (InetSocketAddress)channel.localAddress();
        final var remoteAddress = (InetSocketAddress)channel.remoteAddress();
        final var id = ConnectionId.create(Transport.tcp, localAddress, remoteAddress);
        final var connCtx = findContext(id);

        if (connCtx.isDrop()) {
            ctx.close();
            return;
        }

        // connection = new ConnectionAdapter(new TcpConnection(channel, id, vipAddress), connCtx);

        // TODO: just figured out a better way. The Connection object is ONLY for
        // application at the top since at the end of the day, if you write to the
        // connection you will go down the entire netty pipeline.
        // However, if you insert a handler in the netty pipeline you do want the same
        // behavior as the netty ChannelHandlerContext. Meaning, you have a way to
        // forward the event upstream or "turn around" and push it downstream
        // through the chain of handlers.
        //
        // SO: this TCPInboundAdapter should actually not create a new TCP connection
        // but rather a TcpChannelContext or something. that is then what the
        // FSMs in the "middle" uses and then when we eventually hit the application layer
        // adapter it will create the TcpConnection object that is then given to
        // the actual application, because again, at the application you no longer
        // have any upstream handlers and as such, you can only write.
        // Way better!
        connection = new TcpConnection<T>(channel, id, vipAddress);
        channelContext = new DefaultChannelContext<T>(connection, connCtx);
        final var evt = ConnectionActiveIOEvent.create(channelContext, clock.getCurrentTimeMillis());
        ctx.fireUserEventTriggered(evt);
    }

    private ConnectionContext<Connection<T>, T> findContext(final ConnectionId id) {
        return ctxs.stream().filter(ctx -> ctx.test(id)).findFirst().orElse(defaultCtx);
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        logger.debug("Channel is Inactive {}", ctx.channel());
        if (ctx.channel().localAddress() != null) {
            // final ConnectionIOEvent event = create(ctx, ConnectionInactiveIOEvent::create);
            // ctx.fireUserEventTriggered(event);
        }
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object object) throws Exception {
        try {
            final var msg = (T)object;
            final var evt = MessageIOEvent.create(channelContext, clock.getCurrentTimeMillis(), msg);
            ctx.fireChannelRead(evt);
        } catch (final ClassCastException e) {
            // TODO: this means that the underlying decoder isn't doing it's job...
            e.printStackTrace();
        }
    }


    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext ctx) throws Exception {
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
