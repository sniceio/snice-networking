package io.snice.networking.diameter.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.snice.buffer.Buffer;
import io.snice.codecs.codec.diameter.DiameterHeader;

import java.util.List;

public class DiameterMessageStreamDecoder extends ByteToMessageDecoder {
    private DiameterHeader currentHeader;

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf bytebuf,
                          final List<Object> list) throws Exception {
        if (currentHeader == null) {
            if (bytebuf.readableBytes() < 20) {
                return;
            }

            final byte[] headerBytes = new byte[20];
            bytebuf.readBytes(headerBytes);
            final Buffer headerBuffer = Buffer.of(headerBytes);
            currentHeader = DiameterHeader.frame(headerBuffer.toReadableBuffer());
        }

        // because the total length includes the header, which we've just read.
        final int length = currentHeader.getLength() - 20;
        if (bytebuf.readableBytes() >= length) {
            final byte[] avp = new byte[length];
            bytebuf.readBytes(avp);
            final Buffer avpBuffer = Buffer.of(avp);
            final DiameterHeader header = currentHeader;
            currentHeader = null;
            try {
                throw new RuntimeException("testing the decoder 2 instead");
                // final DiameterMessage diameter = DiameterParser.frame(header, avpBuffer.toReadableBuffer());
                // list.add(diameter);
            } catch (final IndexOutOfBoundsException e) {
                e.printStackTrace();
                currentHeader = null;
            }
        }
    }
}
