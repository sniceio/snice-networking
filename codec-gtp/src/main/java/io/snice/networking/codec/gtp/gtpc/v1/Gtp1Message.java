package io.snice.networking.codec.gtp.gtpc.v1;

import io.snice.buffer.ReadableBuffer;
import io.snice.networking.codec.gtp.GtpMessage;

public interface Gtp1Message extends GtpMessage {

    static Gtp1Message frame(final Gtp1Header header, final ReadableBuffer buffer) {
        throw new RuntimeException("Not yet implemented");
    }

}
