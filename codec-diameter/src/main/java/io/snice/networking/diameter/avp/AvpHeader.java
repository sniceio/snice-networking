package io.snice.networking.diameter.avp;

import io.snice.buffer.ReadableBuffer;
import io.snice.networking.diameter.DiameterParseException;
import io.snice.networking.diameter.impl.DiameterParser;

import java.io.IOException;
import java.util.Optional;

/**
 * The {@link AvpHeader} contains the AVP code, length, flags and potentially
 * the vendor specific ID.
 *
 * @author jonas@jonasborjesson.com
 */
public interface AvpHeader {

    static AvpHeader frame(final ReadableBuffer buffer) throws DiameterParseException, IOException {
        return DiameterParser.frameAvpHeader(buffer);
    }

    /**
     * The length (in bytes) of the header itself.
     *
     * @return
     */
    int getHeaderLength();

    long getCode();

    int getLength();

    Optional<Long> getVendorId();

    boolean isVendorSpecific();

    boolean isMandatory();

    boolean isProtected();
}
