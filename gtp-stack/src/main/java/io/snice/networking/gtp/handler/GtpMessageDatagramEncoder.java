package io.snice.networking.gtp.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.gtp.event.GtpEvent;

import java.util.List;

public class GtpMessageDatagramEncoder extends MessageToMessageEncoder<GtpEvent> {

    @Override
    protected void encode(final ChannelHandlerContext ctx, final GtpEvent event, final List<Object> out) {
        System.err.println("GtpMessageEncoder: " + event);
        if (!event.isMessageWriteEvent()) {
            return;
        }

        try {
            final var write = event.toMessageWriteEvent();
            final var byteBuf = toByteBuf(ctx.channel(), write.getMessage());
            final DatagramPacket pkt = new DatagramPacket(byteBuf, write.getConnection().getRemoteAddress());
            System.err.println("UDP packet: " + pkt);
            out.add(pkt);
        } catch (final Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    public static ByteBuf toByteBuf(final Channel channel, final GtpMessage msg) {
        final Buffer buffer = msg.getBuffer();
        final int capacity = buffer.capacity();
        final ByteBuf byteBuf = channel.alloc().buffer(capacity, capacity);
        for (int i = 0; i < buffer.capacity(); ++i) {
            byteBuf.writeByte(buffer.getByte(i));
        }
        return byteBuf;
    }

}
