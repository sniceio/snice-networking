package io.snice.networking.diameter.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.sctp.SctpMessage;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.snice.buffer.Buffer;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.diameter.event.DiameterEvent;

import java.util.List;

public class DiameterSctpEncoder extends MessageToMessageEncoder<DiameterEvent> {

    @Override
    protected void encode(final ChannelHandlerContext ctx, final DiameterEvent evt, final List<Object> list) throws Exception {
        if (!evt.isMessageEvent()) {
            return;
        }

        final var msg = evt.toMessageEvent().getMessage();
        final var buf = toByteBuf(ctx.channel(), msg);
        final var sctp = new SctpMessage(0, 0, buf);
        list.add(sctp);
    }

    public static ByteBuf toByteBuf(final Channel channel, final DiameterMessage msg) {
        final Buffer buffer = msg.getBuffer();
        final int capacity = buffer.capacity();
        final ByteBuf byteBuf = channel.alloc().buffer(capacity, capacity);
        for (int i = 0; i < buffer.capacity(); ++i) {
            byteBuf.writeByte(buffer.getByte(i));
        }
        return byteBuf;
    }
}
