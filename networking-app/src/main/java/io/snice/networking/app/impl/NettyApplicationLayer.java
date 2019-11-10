package io.snice.networking.app.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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

        System.err.println("And now we need to invoke the application: " + msg);
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        logger.debug("User Event Triggered: " + evt);
        ctx.fireUserEventTriggered(evt);
    }
}
