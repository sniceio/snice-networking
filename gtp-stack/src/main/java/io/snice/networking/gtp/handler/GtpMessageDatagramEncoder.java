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
        if (!event.isMessageWriteEvent()) {
            return;
        }

        final var write = event.toMessageWriteEvent();
        final var byteBuf = toByteBuf(ctx.channel(), write.getMessage());
        final DatagramPacket pkt = new DatagramPacket(byteBuf, write.getConnection().getRemoteAddress());
        out.add(pkt);
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
