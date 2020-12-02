package io.snice.networking.app.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.event.ConnectionAttemptCompletedIOEvent;
import io.snice.networking.common.event.IOEvent;
import io.snice.networking.common.event.MessageIOEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class NettyApplicationLayer<K extends Connection<T>, T, C extends NetworkAppConfig> extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyApplicationLayer.class);
    private final ProtocolBundle<K, T, C> bundle;

    public NettyApplicationLayer(final ProtocolBundle<K, T, C> bundle) {
        this.bundle = bundle;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object object) throws Exception {
        final var event = (MessageIOEvent<T>) object;
        final var msg = event.getMessage();
        final var channelContext = (DefaultChannelContext<T>) event.channelContext();

        invokeApplication(msg, channelContext.getConnectionId(), ctx, channelContext.getConnectionContext());
    }

    private void invokeApplication(final T msg, final ConnectionId id, final ChannelHandlerContext ctx, final ConnectionContext<Connection<T>, T> appRules) {
        // TODO: this needs to take place in a different thread pool!
        // TODO: make it configurable and even allow the customer to use the same thread pool with
        //       a big WARNING sign
        final var bufferingConnection = new BufferingConnection<T>(id);
        final var appConnection = bundle.wrapConnection(bufferingConnection);
        appRules.match(appConnection, msg).apply(appConnection, msg);
        bufferingConnection.processMessage(ctx);
    }

    private void invokeApplicationForEvent(final Object event, final ChannelHandlerContext ctx, final ConnectionContext<Connection<T>, T> appRules) {
        // TODO: this needs to take place in a different thread pool!
        final var bufferingConnection = new BufferingConnection<T>(null);
        final var appConnection = bundle.wrapConnection(bufferingConnection);
        appRules.matchEvent(appConnection, event).apply(appConnection, event);
        bufferingConnection.processMessage(ctx);
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        logger.info("UserEventTriggered: " + evt);
        try {
            final IOEvent<T> ioEvent = (IOEvent<T>) evt;
            if (ioEvent.isConnectionAttemptCompletedIOEvent()) {
                completeConnectionFuture(ioEvent.toConnectionAttemptCompletedIOEvent());
            } else if (ioEvent.isApplicationEvent()) {
                final var appEvent = ioEvent.toApplicationEvent();
                final var channelContext = (DefaultChannelContext<T>) ioEvent.channelContext();
                invokeApplicationForEvent(appEvent.getApplicationEvent(), ctx, channelContext.getConnectionContext());

            } else if (ioEvent.isConnectionActiveIOEvent()) {
                // Not sure we need to do anything here... so for now, not doing anything...
            } else {
                // TODO: log warn with an AlertCode etc...
                logger.warn("Unhandled IOEvent " + ioEvent);
                final var channelContext = (DefaultChannelContext<T>) ioEvent.channelContext();
                invokeApplicationForEvent(evt, ctx, channelContext.getConnectionContext());
            }
        } catch (final ClassCastException e) {
            // TODO: log warn...
            e.printStackTrace();
        }
    }

    private void completeConnectionFuture(final ConnectionAttemptCompletedIOEvent<T> evt) {
        final var future = evt.getUserFuture();
        evt.getConnection().ifPresent(future::complete);
        evt.getCause().ifPresent(future::completeExceptionally);

        // TODO: the cause could be empty even though it is a failed one. Perhaps we should
        // change this to have a getFailureReason() since the connection could have been dropped
        // due to local policy
    }
}
