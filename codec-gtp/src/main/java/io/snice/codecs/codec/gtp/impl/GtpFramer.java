package io.snice.codecs.codec.gtp.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.codecs.codec.gtp.GtpHeader;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2InfoElementType;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.IMSI;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.TypeLengthInstanceValue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * Base class for all things related to framing GTP messages.
 *
 * @author jonas@jonasborjesson.com
 */
public final class GtpFramer {

    public static final Map<Byte, Function<TypeLengthInstanceValue, ? extends TypeLengthInstanceValue>> framers = new HashMap<>();

    static {
        framers.put(Gtp2InfoElementType.IMSI.getType(), tliv -> IMSI.frame(tliv.getValue()));
    }

    public static Function<TypeLengthInstanceValue, ? extends TypeLengthInstanceValue> getFramer(final Byte b) {
        return framers.get(b);
    }

    public static GtpMessage frameGtpMessage(final Buffer buffer) {
        assertNotNull(buffer, "The buffer cannot be null");
        final ReadableBuffer readable = buffer.toReadableBuffer();
        final GtpHeader header = frameGtpHeader(readable);
        return null;
    }

    public static GtpHeader frameGtpHeader(final Buffer buffer) {
        assertNotNull(buffer, "The buffer cannot be null");
        return frameGtpHeader(buffer.toReadableBuffer());
    }

}
