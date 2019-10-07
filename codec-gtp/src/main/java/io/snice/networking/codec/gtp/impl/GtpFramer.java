package io.snice.networking.codec.gtp.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.networking.codec.gtp.GtpHeader;
import io.snice.networking.codec.gtp.GtpMessage;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * Base class for all things related to framing GTP messages.
 *
 * @author jonas@jonasborjesson.com
 */
public final class GtpFramer {

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
