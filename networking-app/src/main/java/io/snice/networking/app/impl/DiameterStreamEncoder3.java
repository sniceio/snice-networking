package io.snice.networking.app.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.snice.buffer.Buffer;
import io.snice.networking.codec.diameter.DiameterMessage;

import java.net.SocketAddress;
import java.util.List;

public class DiameterStreamEncoder3 extends ChannelOutboundHandlerAdapter {

    public DiameterStreamEncoder3() {
        System.err.println("Created 3");
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object o, ChannelPromise channelPromise) throws Exception {
        final Buffer msg = ((DiameterMessage)o).getBuffer();
        final int capacity = msg.capacity();
        final ByteBuf buffer = ctx.alloc().buffer(capacity, capacity);
        for (int i = 0; i < msg.capacity(); ++i) {
            buffer.writeByte(msg.getByte(i));
        }
        ctx.write(buffer);
    }

}
