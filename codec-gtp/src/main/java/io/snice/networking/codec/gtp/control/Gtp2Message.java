package io.snice.networking.codec.gtp.control;

import io.snice.buffer.ReadableBuffer;
import io.snice.networking.codec.gtp.GtpMessage;
import io.snice.networking.codec.gtp.control.impl.Gtp2MessageImpl;

public interface Gtp2Message extends GtpMessage {

    static Gtp2Message frame(final Gtp2Header header, final ReadableBuffer buffer) {
        return Gtp2MessageImpl.frame(header, buffer);
    }


}
