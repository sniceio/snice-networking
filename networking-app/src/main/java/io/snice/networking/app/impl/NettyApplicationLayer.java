package io.snice.networking.app.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.event.ConnectionAttemptCompletedIOEvent;
import io.snice.networking.common.event.IOEvent;
import io.snice.networking.common.event.MessageIOEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;
import java.util.function.Predicate;

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
        try {
            final var bufferingConnection = new BufferingConnection<T>(id);
            final var appConnection = bundle.wrapConnection(bufferingConnection);
            appRules.match(appConnection, msg).apply(appConnection, msg);
            bufferingConnection.processMessage(ctx);
        } catch (final Throwable t) {
            System.err.println("Application threw an exception. Wiill have to deal with it somehow");
            t.printStackTrace();
        }
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
        try {
            final IOEvent<T> ioEvent = (IOEvent<T>) evt;
            if (ioEvent.isConnectionAttemptCompletedIOEvent()) {
                processConnectionEstablished(ioEvent.toConnectionAttemptCompletedIOEvent());
            } else if (ioEvent.isApplicationEvent()) {
                final var appEvent = ioEvent.toApplicationEvent();
                final var channelContext = (DefaultChannelContext<T>) ioEvent.channelContext();
                invokeApplicationForEvent(appEvent.getApplicationEvent(), ctx, channelContext.getConnectionContext());
            } else if (ioEvent.isConnectionActiveIOEvent()) {
                // Not sure we need to do anything here... so for now, not doing anything...
                logger.info("Connection is now active " + ioEvent.channelContext().getConnectionId());
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

    /**
     * Once a connection has successfully been established, we need to let the user application know, which
     * can happen in two different ways depending if this was a user initiated connection (as in, the user
     * asked us to initate a new outbound connection) or if this was an incoming connection.
     * <p>
     * For user initiated connections (again, outbound connections), the user will be given a
     * {@link CompletionStage} that we will complete below once the connection has been successfully established
     * and we are doing so below.
     * <p>
     * For inbound connections we will first evaluate the rules of whether the user even wants to accept
     * this incoming connection (See {@link NetworkBootstrap#onConnection(Predicate)}) and assuming the user
     * would like to accept the incoming connection, they can also ask to save the connection and supply a
     * "save function". That save function, if present, will be called for the inbound connection use case.
     *
     * @param evt
     */
    private void processConnectionEstablished(final ConnectionAttemptCompletedIOEvent<T> evt) {
        final var future = evt.getUserFuture();
        if (future != null) {
            evt.getConnection().ifPresent(c -> {
                final var specializedConnection = bundle.wrapConnection(c);
                future.complete(specializedConnection);
            });
            evt.getCause().ifPresent(future::completeExceptionally);
        } else {
            final var internalCtxt = (InternalChannelContext<T>) evt.channelContext();
            final var saveAction = internalCtxt.getConnectionContext().getSaveAction();
            evt.getConnection().ifPresent(c -> saveAction.ifPresent(f -> {
                        final var appConnection = bundle.wrapConnection(c);
                        f.accept(appConnection);
                    }
            ));
        }

        // TODO: the cause could be empty even though it is a failed one. Perhaps we should
        // change this to have a getFailureReason() since the connection could have been dropped
        // due to local policy
    }
}
