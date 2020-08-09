package io.snice.networking.diameter.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.diameter.event.DiameterEvent;

public class DiameterStreamEncoder extends MessageToByteEncoder<DiameterEvent> {

    @Override
    protected void encode(final ChannelHandlerContext channelHandlerContext, final DiameterEvent evt, final ByteBuf byteBuf) throws Exception {
        if (!evt.isMessageEvent()) {
            return;
        }

        final var msg = evt.toMessageEvent().getMessage();

        // TODO: this is so dumb. We can do better.
        final var buffer = msg.getBuffer();
        for (int i = 0; i < buffer.capacity(); ++i) {
            byteBuf.writeByte(buffer.getByte(i));
        }
    }
}
