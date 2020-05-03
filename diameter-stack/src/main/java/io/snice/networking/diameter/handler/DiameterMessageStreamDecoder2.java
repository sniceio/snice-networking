package io.snice.networking.diameter.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.snice.buffer.Buffer;
import io.snice.codecs.codec.diameter.impl.DiameterParser;

import java.util.List;

public class DiameterMessageStreamDecoder2 extends ByteToMessageDecoder {

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf bytebuf,
                          final List<Object> list) throws Exception {

        if (bytebuf.readableBytes() < 20) {
            return;
        }

        final int length = Buffer.signedInt(bytebuf.getByte(1), bytebuf.getByte(2), bytebuf.getByte(3));

        if (bytebuf.readableBytes() < length) {
            return;
        }

        final byte[] raw = new byte[length];
        bytebuf.readBytes(raw);
        final Buffer buffer = Buffer.of(raw);
        try {
            list.add(DiameterParser.frame(buffer));
        } catch (final IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
}
