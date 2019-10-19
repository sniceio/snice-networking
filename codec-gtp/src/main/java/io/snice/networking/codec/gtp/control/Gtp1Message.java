package io.snice.networking.codec.gtp.control;

import io.snice.buffer.ReadableBuffer;
import io.snice.networking.codec.gtp.GtpMessage;
import io.snice.networking.codec.gtp.control.impl.Gtp2MessageImpl;

public interface Gtp1Message extends GtpMessage {

    static Gtp1Message frame(final Gtp1Header header, final ReadableBuffer buffer) {
        throw new RuntimeException("Not yet implemented");
    }

}
