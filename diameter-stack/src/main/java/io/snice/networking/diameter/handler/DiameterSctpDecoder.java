package io.snice.networking.diameter.handler;

import com.sun.nio.sctp.Association;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.sctp.SctpMessage;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.diameter.event.DiameterMessageReadEvent;

import java.util.List;

/**
 * Note that his handler is NOT sharable!
 */
public class DiameterSctpDecoder extends MessageToMessageDecoder<SctpMessage> {

    private final SctpStream[] streams = new SctpStream[100];

    @Override
    protected void decode(final ChannelHandlerContext ctx, final SctpMessage sctp, final List<Object> list) throws Exception {
        final var info = sctp.messageInfo();
        final var stream = ensureStream(info.association(), info.streamNumber());
        final var aggregate = stream.aggregate(ctx, sctp);
        if (aggregate == null) {
            return;
        }

        try {
            final var buffer = aggregate.toReadableBuffer();
            final var diameter = DiameterMessage.frame(buffer);
            final var evt = DiameterMessageReadEvent.of(diameter);
            list.add(evt);
            if (buffer.hasReadableBytes()) {
                if (buffer.getReadableBytes() < 20) {
                    // can't be Diameter
                    return;
                }

                final var headerMaybe = buffer.readBytes(20);
                if (headerMaybe.countOccurences(0, 20, (byte) 0x00) == 20) { // only zeros so empty buffer.
                    return;
                }

                throw new RuntimeException("Turns out it wasn't only zeros... really???");
            }
        } catch (final Throwable t) {
            t.printStackTrace();
            System.err.println("SCTP message info " + sctp.messageInfo());
            throw t;
        }
    }

    private SctpStream ensureStream(final Association association, final int streamNumber) {
        final var stream = streams[streamNumber];
        if (streams[streamNumber] != null) {
            return stream;
        }

        final var newStream = new SctpStream(association, streamNumber);
        streams[streamNumber] = newStream;
        return newStream;
    }

    private static class SctpStream {

        // TODO: make configurable
        private static final int MAX_SEGMENTS = 10;

        private final Association association;
        private final int streamNumber;

        /**
         * Store all byte
         */
        private final ByteBuf[] buffers = new ByteBuf[MAX_SEGMENTS];
        private int totalSize = 0;
        private int index = 0;

        private SctpStream(final Association association, final int streamNumber) {
            this.association = association;
            this.streamNumber = streamNumber;
        }

        private void ensureMaxSegments() {
            if (index < MAX_SEGMENTS) {
                return;
            }
            for (int i = 0; i < index; ++i) {
                final var buffer = buffers[i];
                buffer.release();
                buffers[i] = null;
            }

            // TODO: probably want to allow the application to handle the segments
            // in some way. Perhaps they want to shut down the connection or maybe not
            throw new RuntimeException("Too many SCTP segments");
        }

        /**
         *
         */
        protected Buffer aggregate(final ChannelHandlerContext ctx, final SctpMessage sctp) throws Exception {
            final var info = sctp.messageInfo();
            if (!info.isComplete()) {
                ensureMaxSegments();
                final var buffer = sctp.content().retain();
                buffers[index++] = buffer;
                totalSize += buffer.readableBytes();
                return null;
            }

            final var currentContent = sctp.content();
            final byte[] b = new byte[totalSize + currentContent.readableBytes()];
            int writeIndex = 0;
            for (int i = 0; i < index; ++i) {
                final var buffer = buffers[i];
                buffer.getBytes(0, b, writeIndex, buffer.readableBytes());
                writeIndex += buffer.readableBytes();
                buffer.release();
                buffers[i] = null;
            }

            index = 0;
            currentContent.getBytes(0, b, writeIndex, currentContent.readableBytes());
            return Buffers.wrap(b);
        }
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
