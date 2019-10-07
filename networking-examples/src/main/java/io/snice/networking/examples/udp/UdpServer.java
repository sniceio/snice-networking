package io.snice.networking.examples.udp;

import io.netty.channel.ChannelHandlerContext;
import io.snice.networking.examples.Utils;
import io.snice.networking.netty.InboundOutboundHandlerAdapter;
import io.snice.networking.netty.NettyNetworkLayer;

/**
 * A simple example of a UDP server
 */
public class UdpServer extends InboundOutboundHandlerAdapter {

    public static void main(final String... args) throws Exception {
        final UdpServerConfig config = Utils.loadConfiguration(UdpServerConfig.class, "UdpServerConfig.yml");
        final var network = NettyNetworkLayer.with(config.getNetworkConfiguration())
                .withHandler("my_server", new UdpServer())
                .build();

        network.start();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        System.err.println(msg);
        ctx.fireChannelRead(msg);
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        System.out.println("yay");

    }

    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
        System.out.println("yay");

    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        System.out.println("oops");

    }
}
