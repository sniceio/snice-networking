package io.snice.networking.diameter.handler;

import com.sun.nio.sctp.Association;
import com.sun.nio.sctp.AssociationChangeNotification;
import com.sun.nio.sctp.Notification;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.sctp.SctpMessage;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.AttributeKey;
import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.diameter.event.DiameterMessageReadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Note that his handler is NOT sharable!
 */
public class DiameterSctpDecoder extends MessageToMessageDecoder<SctpMessage> {

    public static final AttributeKey<Association> ASSOCIATION_KEY = AttributeKey.valueOf(DiameterSctpDecoder.class, "association");

    /**
     * The max number of inbound streams we want to support. This should be negotiated
     * during the SCTP handshake but as an extra precaution, we will also cap it here.
     */
    private static final int MAX_INBOUND_STREAMS = 100;

    private static final Logger logger = LoggerFactory.getLogger(DiameterSctpDecoder.class);

    private final UUID uuid = UUID.randomUUID();

    /**
     * The total number of streams is negotiated during the SCTP handshake. The max inbound
     * stream is read from that but there is also an absolute max we want to use so we will
     * cap it if need be (TODO: should be configurable and set for the SCTP handshake. Verify that)
     */
    private SctpStream[] streams;

    /**
     * When the channel becomes active, this MUST have been set and should have been communicated
     * by the stack to this handler via user events. The fact that the association is
     * existing is checked in {@link #channelActive(ChannelHandlerContext)} and if that happens
     * not to be true, the connection is closed (and this would be a bug in our code somewhere, most likelyu
     * this class or Netty has changed)
     */
    private Association association;


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

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        log("Channel active");
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

            if (index != 0) {
                for (int i = 0; i < index; ++i) {
                    final var buffer = buffers[i];
                    buffer.getBytes(0, b, writeIndex, buffer.readableBytes());
                    writeIndex += buffer.readableBytes();
                    buffer.release();
                    buffers[i] = null;
                }
                index = 0;
            }

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

    private void log(final String msg) {
        logger.info("[ " + uuid + "]: " + msg);
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        log("UserEventTriggered: " + evt);
        if (evt instanceof Notification) {
            processSctpNotificationEvent(ctx, (Notification) evt);
        }


        ctx.fireUserEventTriggered(evt);
    }

    private void processSctpNotificationEvent(final ChannelHandlerContext ctx, final Notification notification) {
        final var association = notification.association();
        if (notification instanceof AssociationChangeNotification) {
            final var change = (AssociationChangeNotification) notification;
            switch (change.event()) {
                case COMM_UP:
                    processAssociationUp(ctx, change);
                    break;
                case COMM_LOST:
                    log("The communication is lost for association " + association);
                    break;
                case RESTART:
                    log("The communication restarted for association " + association);
                    break;
                case SHUTDOWN:
                    log("The communication shutdown for association " + association);
                    break;
                case CANT_START:
                    log("The communication can't start for association " + association);
                    break;
                default:
                    break;
            }
        }
    }

    private void processAssociationUp(final ChannelHandlerContext ctx, final AssociationChangeNotification event) {
        association = event.association();
        final var maxInboundStreams = Math.min(association.maxInboundStreams(), MAX_INBOUND_STREAMS);
        streams = new SctpStream[maxInboundStreams];
        ctx.channel().attr(ASSOCIATION_KEY).set(association);

        log("Configuring the association " + association.associationID()
                + " with max inbound streams of " + maxInboundStreams);
    }
}
