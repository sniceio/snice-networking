package io.snice.networking.diameter.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.snice.networking.codec.diameter.DiameterMessage;

public class DiameterStreamEncoder extends MessageToByteEncoder<DiameterMessage> {

    @Override
    protected void encode(final ChannelHandlerContext channelHandlerContext, final DiameterMessage diameterMessage, final ByteBuf byteBuf) throws Exception {
        // TODO: this is so dumb. We can do better.
        final var buffer = diameterMessage.getBuffer();
        if (diameterMessage.isULA()) {
            System.err.println("doing it");
        }
        for (int i = 0; i < buffer.capacity(); ++i) {
            byteBuf.writeByte(buffer.getByte(i));
        }
    }
}
