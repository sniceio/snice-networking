package io.snice.networking.app.impl;

import com.sun.nio.sctp.AssociationChangeNotification;
import com.sun.nio.sctp.Notification;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.common.event.ConnectionActiveIOEvent;
import io.snice.networking.common.event.ConnectionAttemptCompletedIOEvent;
import io.snice.networking.common.event.IOEvent;
import io.snice.networking.common.event.MessageIOEvent;
import io.snice.networking.core.event.ConnectionAttempt;
import io.snice.networking.core.event.ConnectionAttemptFailed;
import io.snice.networking.core.event.NetworkEvent;
import io.snice.networking.netty.SctpConnection;
import io.snice.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.snice.preconditions.PreConditions.assertNull;

/**
 * NOTE: this class is not sharable. If you do not know what that means, read up on
 * the Netty ChannelHandlers and @Sharable (the netty annotation). In short, each
 * channel will have it's own handler because it stores states that is unique
 * to only that channel and as such, it cannot be shared...
 */
public class NettySctpInboundAdapter<T> extends ChannelOutboundHandlerAdapter implements ChannelInboundHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettySctpInboundAdapter.class);

    private final Clock clock;
    private final Optional<URI> vipAddress;

    // TODO: don't really need this one.
    private final UUID uuid = UUID.randomUUID();

    private final List<ConnectionContext> ctxs;
    private final int exceptionCounter = 0;

    /**
     * Flag to indicate if this channel was initiated by an incoming connection
     * or whether we were the one initiating it, i.e. we tried to establish an outgoing TCP connection.
     */
    private boolean isInbound = true;

    /**
     * If the incoming connection doesn't match anything, then we'll use this
     * default context instead.
     */
    private final ConnectionContext defaultCtx;

    private SctpConnection<T> connection;
    private DefaultChannelContext<T> channelContext;

    /**
     * We'll hold on to the connection attempt event until we've seen the
     * {@link ConnectionActiveIOEvent} event.
     */
    private ConnectionAttempt<T> connectionAttempt;

    private boolean hasProcessedConnectionActiveIOEvent = false;

    public NettySctpInboundAdapter(final Clock clock, final Optional<URI> vipAddress, final List<ConnectionContext> ctxs) {
        this.clock = clock;
        this.vipAddress = vipAddress;
        this.ctxs = ctxs;

        // TODO
        defaultCtx = null;
    }

    private void log(final String msg) {
        logger.info("[ " + uuid + " SCTP ]: " + msg);
    }

    private void logError(final String msg) {
        logError("[ " + uuid + "]: " + msg);
    }

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        log("Channel registered for inbound");
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
        log("Channel unregistered for inbound");
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
        log("Channel active");
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
            final var msg = (T) object;
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
        log("UserEventTriggered: " + evt);

        try {
            List<IOEvent<T>> events = List.of();
            if (evt instanceof Notification) {
                events = processSctpNotificationEvent(ctx, (Notification) evt);
            } else {
                final NetworkEvent<T> event = (NetworkEvent<T>) evt;
                if (event.isConnectionAttemptEvent()) {
                    final var e = processConnectionAttemptCompleted(false, ctx, event.toConnectionAttempt());
                    if (e.isPresent()) {
                        events = List.of(e.get());
                    }
                }
            }


            events.forEach(ctx::fireUserEventTriggered);
            ctx.flush();

        } catch (final ClassCastException e) {
            // TODO: need to introduce proper logging and this should
            // be a WARN with an AlertCode.
            logError("Unknown user event triggered. Dropping event " + evt.getClass().getName() + " toString: " + evt);
        }
    }

    private List<IOEvent<T>> processSctpNotificationEvent(final ChannelHandlerContext ctx, final Notification notification) {
        final var association = notification.association();
        if (notification instanceof AssociationChangeNotification) {
            final var change = (AssociationChangeNotification) notification;
            switch (change.event()) {
                case COMM_UP:
                    return processAssociationUp(ctx, change);
                case COMM_LOST:
                    System.out.println("The communication is lost for association " + association);
                case RESTART:
                    System.out.println("The communication restarted for association " + association);
                case SHUTDOWN:
                    System.out.println("The communication shutdown for association " + association);
                case CANT_START:
                    System.out.println("The communication can't start for association " + association);
            }
        }
        return List.of();
    }

    private List<IOEvent<T>> processAssociationUp(final ChannelHandlerContext ctx, final AssociationChangeNotification event) {
        assertNull(connection, "There was already an active connection for this SCTP Context, should be impossible.");
        log("Channel is now active. IsInbound: " + isInbound);

        final var channel = ctx.channel();
        final var localAddress = (InetSocketAddress) channel.localAddress();
        final var remoteAddress = (InetSocketAddress) channel.remoteAddress();
        final var id = ConnectionId.create(Transport.sctp, localAddress, remoteAddress);
        final var connCtx = findContext(id);

        hasProcessedConnectionActiveIOEvent = true;

        if (connCtx.isDrop()) {
            // TODO: need to actually call the potential drop function too!
            // TODO: and the app may actually issue a message of some sort that needs to be written to
            // TODO: the socket too...
            ctx.close();
            return List.of();
        }

        connection = new SctpConnection<T>(channel, id, vipAddress);
        channelContext = new DefaultChannelContext<T>(connection, connCtx);
        final var connectionEvt = ConnectionActiveIOEvent.create(channelContext, isInbound, clock.getCurrentTimeMillis());
        final var completedEvt = processConnectionAttemptCompleted(false, ctx, connectionAttempt);
        if (completedEvt.isPresent()) {
            return List.of(connectionEvt, completedEvt.get());
        }

        return List.of(connectionEvt);
    }

    /**
     * When the user tries to establish a new connection they will be given a future that will eventually
     * complete. Our implementation wants to ensure that the {@link ConnectionActiveIOEvent} is propagated before
     * this event, which may or may not be the case, and as such, we will ensure to potentially hold onto the
     * low-level {@link ConnectionAttempt} event and only process and propagate it up the chain once the
     * {@link ConnectionActiveIOEvent} has been processed and propagated.
     *
     * @param isDropped whether the connection was dropped or not. Will ONLY happen if the filter that
     *                  the user application installed decides to drop the connection. If the connection
     *                  fails to connect (remote host doesn't exist, remote port unreachable etc) then Netty
     *                  will not even create a {@link ChannelHandlerContext} so the {@link ConnectionAttemptFailed}
     *                  is processed differently.
     * @param ctx       the netty context.
     * @param evt       the low-level connection attempt event. It contains the user's future that
     *                  will be completed at some point.
     */
    private Optional<IOEvent<T>> processConnectionAttemptCompleted(final boolean isDropped, final ChannelHandlerContext ctx, final ConnectionAttempt<T> evt) {
        if (evt == null) {
            return Optional.empty();
        }

        if (!hasProcessedConnectionActiveIOEvent) {
            connectionAttempt = evt;
        } else {
            connectionAttempt = null;
            final var e = ConnectionAttemptCompletedIOEvent.create(channelContext, evt.getUserConnectionFuture(), connection, evt.getArrivalTime());
            return Optional.of(e);
        }
        return Optional.empty();
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void connect(final ChannelHandlerContext ctx, final SocketAddress remoteAddress,
                        final SocketAddress localAddress, final ChannelPromise promise) throws Exception {
        isInbound = false;
        ctx.connect(remoteAddress, localAddress, promise);
    }

}
