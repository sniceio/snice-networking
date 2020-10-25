package io.snice.networking.diameter.handler;

import com.sun.nio.sctp.Association;
import com.sun.nio.sctp.MessageInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.sctp.SctpMessage;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.snice.buffer.Buffer;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.diameter.event.DiameterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

public class DiameterSctpEncoder extends MessageToMessageEncoder<DiameterEvent> {

    /**
     * The max number of outbound streams we want to support. This should be negotiated
     * during the SCTP handshake but as an extra precaution, we will also cap it here.
     */
    private static final int MAX_OUTBOUND_STREAMS = 100;

    private static final Logger logger = LoggerFactory.getLogger(DiameterSctpDecoder.class);

    private Association association;
    private int maxOutboundStreams = MAX_OUTBOUND_STREAMS;
    private final Random random = new Random();

    @Override
    protected void encode(final ChannelHandlerContext ctx, final DiameterEvent evt, final List<Object> list) throws Exception {
        if (!evt.isMessageEvent()) {
            return;
        }

        ensureAssociation(ctx);

        final var msg = evt.toMessageEvent().getMessage();
        final var buf = toByteBuf(ctx.channel(), msg);
        final var info = MessageInfo.createOutgoing(association, null, calculateStream(msg));
        final var sctp = new SctpMessage(info, buf);
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

    private int calculateStream(final DiameterMessage msg) {
        final int i = random.nextInt(maxOutboundStreams);
        return i;
    }

    private void ensureAssociation(ChannelHandlerContext ctx) {
        if (association != null) {
            return;
        }

        association = ctx.channel().attr(DiameterSctpDecoder.ASSOCIATION_KEY).get();
        if (association == null) {
            logger.warn("Association not present on the channel. Closing channel");
            ctx.close();
        }
        maxOutboundStreams = Math.min(association.maxOutboundStreams(), MAX_OUTBOUND_STREAMS);
    }

}
