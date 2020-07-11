package io.snice.networking.app.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.snice.networking.app.AppBundle;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.event.ConnectionAttemptCompletedIOEvent;
import io.snice.networking.common.event.IOEvent;
import io.snice.networking.common.event.MessageIOEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class NettyApplicationLayer<K extends Connection<T>, T> extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyApplicationLayer.class);
    private final AppBundle<K, T> bundle;

    public NettyApplicationLayer(final AppBundle<K, T> bundle) {
        this.bundle = bundle;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object object) throws Exception {
        final var event = (MessageIOEvent<T>) object;
        final var msg = event.getMessage();
        final var channelContext = (DefaultChannelContext<T>) event.channelContext();

        invokeApplication(msg, ctx, channelContext.getConnectionContext());
    }

    private void invokeApplication(final T msg, final ChannelHandlerContext ctx, final ConnectionContext<Connection<T>, T> appRules) {
        // TODO: this needs to take place in a different thread pool!
        // TODO: should we here potentially convert the Connection to an application specific connection
        // TODO: object? For Diameter that would be a peer.
        // TODO:
        // TODO:
        final var bufferingConnection = new BufferingConnection<T>(null);
        final var appConnection = bundle.wrapConnection(bufferingConnection);
        appRules.match(appConnection, msg).apply(appConnection, msg);
        bufferingConnection.processMessage(ctx);
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        logger.info("UserEventTriggered: " + evt);
        try {
            final IOEvent<T> ioEvent = (IOEvent<T>)evt;
            if (ioEvent.isConnectionAttemptCompletedIOEvent()) {
                completeConnectionFuture(ioEvent.toConnectionAttemptCompletedIOEvent());
            } else if (ioEvent.isConnectionActiveIOEvent()) {
                logger.info("ConnectionActiveIOEvent - turn into a Peer connection here?");
            } else {
                // TODO: log warn with an AlertCode etc...
                logger.warn("Unhandled IOEvent " + ioEvent);
            }
        } catch (final ClassCastException e) {
            // TODO: log warn...
            e.printStackTrace();
        }
    }

    private void completeConnectionFuture(final ConnectionAttemptCompletedIOEvent<T> evt) {
        logger.info("CompleteConnectionFuture: " + evt);
        final var future = evt.getUserFuture();
        evt.getConnection().ifPresent(future::complete);
        evt.getCause().ifPresent(future::completeExceptionally);

        // TODO: the cause could be empty even though it is a failed one. Perhaps we should
        // change this to have a getFailureReason() since the connection could have been dropped
        // due to local policy
    }
}
