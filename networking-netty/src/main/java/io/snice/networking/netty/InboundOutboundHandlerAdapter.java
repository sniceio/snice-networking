package io.snice.networking.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

/**
 * Combined inbound and outbound handler. When moving over to Netty 5, we should be able to
 * remove this one...
 *
 * @author jonas@jonasborjesson.com
 */
public class InboundOutboundHandlerAdapter implements ChannelInboundHandler, ChannelOutboundHandler {

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelRegistered();
    }
    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelUnregistered();

    }
    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        ctx.fireChannelRead(msg);
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelReadComplete();
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        ctx.fireUserEventTriggered(evt);
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelWritabilityChanged();
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }

    /**
     * From ChannelHandler
     */
    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
    }

    /**
     * From ChannelHandler
     */
    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
    }

    /**
     * From ChannelOutboundHandler
     */
    @Override
    public void bind(final ChannelHandlerContext ctx, final SocketAddress localAddress, final ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    /**
     * From ChannelOutboundHandler
     */
    @Override
    public void connect(final ChannelHandlerContext ctx, final SocketAddress remoteAddress, final SocketAddress localAddress, final ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    /**
     * From ChannelOutboundHandler
     */
    @Override
    public void disconnect(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        ctx.disconnect(promise);
    }

    /**
     * From ChannelOutboundHandler
     */
    @Override
    public void close(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        ctx.close(promise);
    }

    /**
     * From ChannelOutboundHandler
     */
    @Override
    public void deregister(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }

    /**
     * From ChannelOutboundHandler
     */
    @Override
    public void read(final ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    /**
     * From ChannelOutboundHandler
     */
    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
        ctx.write(msg, promise);
    }

    /**
     * From ChannelOutboundHandler
     */
    @Override
    public void flush(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
