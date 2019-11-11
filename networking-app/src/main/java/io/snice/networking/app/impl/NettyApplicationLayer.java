package io.snice.networking.app.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.event.MessageIOEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class NettyApplicationLayer<T> extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyApplicationLayer.class);

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object object) throws Exception {
        final var event = (MessageIOEvent<T>) object;
        final var msg = event.getMessage();
        final var channelContext = (DefaultChannelContext<T>) event.channelContext();

        invokeApplication(msg, ctx, channelContext.getConnectionContext());
    }

    private void invokeApplication(final T msg, final ChannelHandlerContext ctx, final ConnectionContext<Connection<T>, T> appRules) {
        final var bufferingConnection = new BufferingConnection<T>(null);
        appRules.match(bufferingConnection, msg).apply(bufferingConnection, msg);
        bufferingConnection.processMessage(ctx);
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        logger.debug("User Event Triggered: " + evt);
        ctx.fireUserEventTriggered(evt);
    }
}
