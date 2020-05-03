package io.snice.codecs.codec.gtp.gtpc.v2.tliv.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.codecs.codec.gtp.GtpParseException;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.TypeLengthInstanceValue;
import io.snice.codecs.codec.gtp.impl.GtpFramer;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * The {@link RawTypeLengthInstanceValue} represents a framed, but otherwise unparsed,
 * TLIV. In many cases, an application dealing with GTP does not need to check every
 * IE (information element) in detail and as such, can essentially leave it alone
 * and may e.g. write it back out to socket raw. If, however, the application
 * need to parse the value out, then use the {@link #ensure()} method, which
 * will force it to be fully parsed.
 */
public class RawTypeLengthInstanceValue implements TypeLengthInstanceValue {

    private static final byte EXTENSION_TYPE = (byte) 0xFE;

    private final Buffer header;
    private final Buffer value;

    protected RawTypeLengthInstanceValue(final Buffer header, final Buffer value) {
        this.header = header;
        this.value = value;
    }

    public static TypeLengthInstanceValue frame(final Buffer buffer) {
        assertNotNull(buffer, "The buffer cannot be null");
        return frame(buffer.toReadableBuffer());
    }

    public static TypeLengthInstanceValue frame(final ReadableBuffer buffer) {
        assertNotNull(buffer, "The buffer cannot be null");
        assertArgument(buffer.getReadableBytes() >= 4, "A GTPv2 TLIV has at least 4 bytes");

        final byte type = buffer.getByte(buffer.getReaderIndex());
        if (type == EXTENSION_TYPE) {
            throw new RuntimeException("Haven't implemented the extension type just yet");
        }


        final Buffer header = buffer.readBytes(4);
        final int length = header.getUnsignedShort(1);
        final Buffer value = buffer.readBytes(length);

        return new RawTypeLengthInstanceValue(header, value);
    }

    @Override
    public byte getType() {
        return header.getByte(0);
    }

    @Override
    public int getLength() {
        return header.getUnsignedShort(1);
    }

    @Override
    public Buffer getValue() {
        return value;
    }

    /**
     * If this method actually gets called then that means that we are the
     * {@link RawTypeLengthInstanceValue} itself and that we need to frame it
     * further. Subclasses MUST override this method and simply return <code>this</code>
     *
     * @return
     */
    @Override
    public TypeLengthInstanceValue ensure() {
        final var framer = GtpFramer.getFramer(getType());
        if (framer != null) {
            return framer.apply(this);
        }

        // throw something?
        throw new GtpParseException("Unknown GTP IE value");
    }
}
