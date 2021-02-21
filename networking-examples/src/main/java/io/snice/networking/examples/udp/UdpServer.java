package io.snice.networking.examples.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.snice.networking.examples.Utils;
import io.snice.networking.netty.InboundOutboundHandlerAdapter;
import io.snice.networking.netty.NettyNetworkLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple example of a UDP server
 */
public class UdpServer extends InboundOutboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(UdpServer.class);

    public static void main(final String... args) throws Exception {
        final UdpServerConfig config = Utils.loadConfiguration(UdpServerConfig.class, "UdpServerConfig.yml");
        final var network = NettyNetworkLayer.with(config.getNetworkConfiguration())
                .withHandler("my_server", new UdpServer())
                .build();

        network.start();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final var udp = (DatagramPacket) msg;
        final DatagramPacket pkt = new DatagramPacket(udp.content(), udp.sender());
        ctx.write(pkt);
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        System.out.println("oops");
    }
}
