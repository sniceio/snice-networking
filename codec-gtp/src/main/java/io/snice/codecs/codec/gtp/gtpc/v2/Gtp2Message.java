package io.snice.codecs.codec.gtp.gtpc.v2;

import io.snice.buffer.ReadableBuffer;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v2.Impl.Gtp2MessageImpl;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.TypeLengthInstanceValue;

import java.util.Optional;

public interface Gtp2Message extends GtpMessage {

    static Gtp2Message frame(final Gtp2Header header, final ReadableBuffer buffer) {
        return Gtp2MessageImpl.frame(header, buffer);
    }

    Gtp2MessageType getType();

    Optional<? extends TypeLengthInstanceValue> getInformationElement(final Gtp2InfoElementType type);

    /**
     * Convenience method for checking if this message is a Create Session Request or not.
     *
     * @return
     */
    default boolean isCreateSessionRequest() {
        return getType() == Gtp2MessageType.CREATE_SESSION_REQUEST;
    }

    default boolean isCreateSessionResponse() {
        return getType() == Gtp2MessageType.CREATE_SESSION_RESPONSE;

    }

    @Override
    default Gtp2Message toGtp2Message() {
        return this;
    }

}
