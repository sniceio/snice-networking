package io.snice.networking.codec.gtp.gtpc.v2;

import io.snice.buffer.ReadableBuffer;
import io.snice.networking.codec.gtp.GtpMessage;
import io.snice.networking.codec.gtp.gtpc.v2.Impl.Gtp2MessageImpl;

public interface Gtp2Message extends GtpMessage {

    static Gtp2Message frame(final Gtp2Header header, final ReadableBuffer buffer) {
        return Gtp2MessageImpl.frame(header, buffer);
    }

    Gtp2MessageType getType();

    /**
     * Convenience method for checking if this message is a Create Session Request or not.
     *
     * @return
     */
    default boolean isCreateSessionRequest() {
        return getType()== Gtp2MessageType.Create_Session_Request;
    }


}
