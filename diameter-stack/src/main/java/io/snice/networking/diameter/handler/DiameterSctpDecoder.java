package io.snice.networking.diameter.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.sctp.SctpMessage;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.diameter.event.DiameterMessageReadEvent;

import java.util.List;

public class DiameterSctpDecoder extends MessageToMessageDecoder<SctpMessage> {

    @Override
    protected void decode(final ChannelHandlerContext ctx, final SctpMessage sctp, final List<Object> list) throws Exception {
        final var info = sctp.messageInfo();
        final var content = sctp.content();
        final byte[] b = new byte[content.readableBytes()];
        content.getBytes(0, b);

        final var buffer = Buffers.wrap(b);
        final var diameter = DiameterMessage.frame(buffer);
        final var evt = DiameterMessageReadEvent.of(diameter);
        list.add(evt);
        info.complete(true);
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
