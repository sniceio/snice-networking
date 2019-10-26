package io.snice.networking.codec.diameter;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.diameter.impl.DiameterParser;

import java.io.IOException;
import java.util.Optional;

/**
 * The {@link AvpHeader} contains the AVP code, length, flags and potentially
 * the vendor specific ID.
 *
 * @author jonas@jonasborjesson.com
 */
public interface AvpHeader {

    static AvpHeader frame(final Buffer buffer) throws DiameterParseException, IOException {
        return DiameterParser.frameAvpHeader(buffer);
    }

    int getCode();

    long getLength();

    Optional<Long> getVendorId();
}
