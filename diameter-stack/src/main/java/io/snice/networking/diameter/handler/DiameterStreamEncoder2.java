package io.snice.networking.diameter.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.snice.buffer.Buffer;
import io.snice.networking.codec.diameter.DiameterMessage;

import java.util.List;

public class DiameterStreamEncoder2 extends MessageToMessageEncoder<DiameterMessage> {

    public DiameterStreamEncoder2() {
        System.err.println("Created 2");
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final DiameterMessage diameterMessage, final List<Object> list) throws Exception {
        final Buffer msg = diameterMessage.getBuffer();
        final int capacity = msg.capacity();
        final ByteBuf buffer = ctx.alloc().buffer(capacity, capacity);
        for (int i = 0; i < msg.capacity(); ++i) {
            buffer.writeByte(msg.getByte(i));
        }
        list.add(buffer);
    }
}
