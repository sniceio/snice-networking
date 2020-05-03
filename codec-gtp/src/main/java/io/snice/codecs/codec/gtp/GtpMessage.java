package io.snice.codecs.codec.gtp;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.codecs.codec.gtp.gtpc.InfoElement;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2InfoElementType;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2MessageType;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.IMSI;

import java.util.List;
import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 *
 */
public interface GtpMessage {

    static GtpMessage frame(final Buffer buffer) throws GtpParseException, IllegalArgumentException {
        assertNotNull(buffer, "The buffer cannot be null");
        return frame(buffer.toReadableBuffer());
    }

    static GtpMessage frame(final ReadableBuffer buffer) throws GtpParseException, IllegalArgumentException {
        final GtpHeader header = GtpHeader.frame(buffer);
        switch (header.getVersion()) {
            case 1:
                throw new RuntimeException("Not implemented yet");
            case 2:
                return Gtp2Message.frame(header.toGtp2Header(), buffer);
            default:
                // should not happen since the GTP Header should have complained but,
                // it is good practice to have a default path defined
                throw new GtpParseException(buffer.getReaderIndex(), "Unknown GTP protocol version");
        }
    }

    default boolean isRequest() {
        return false;
    }

    default boolean isResponse() {
        return false;
    }

    default boolean isGtpVersion1() {
        return getVersion() == 1;
    }

    default boolean isGtpVersion2() {
        return getVersion() == 2;
    }

    default Optional<IMSI> getImsi() {
        return (Optional<IMSI>) toGtp2Message().getInformationElement(Gtp2InfoElementType.IMSI);
    }

    default Gtp2Message toGtp2Message() {
        throw new ClassCastException("Unable to cast a " + getClass().getName() + " into a " + Gtp2Message.class.getName());
    }

    default boolean isEchoRequest() {
        return getMessageTypeDecimal() == Gtp2MessageType.ECHO_REQUEST.getType();
    }

    GtpHeader getHeader();

    List<? extends InfoElement> getInfoElements();

    default int getMessageTypeDecimal() {
        return getHeader().getMessageTypeDecimal();
    }

    default int getVersion() {
        return getHeader().getVersion();
    }

}
