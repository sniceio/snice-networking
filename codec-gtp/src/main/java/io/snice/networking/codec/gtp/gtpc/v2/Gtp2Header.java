package io.snice.networking.codec.gtp.gtpc.v2;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.networking.codec.gtp.GtpHeader;
import io.snice.networking.codec.gtp.GtpParseException;
import io.snice.networking.codec.gtp.GtpVersionException;
import io.snice.networking.codec.gtp.Teid;
import io.snice.networking.codec.gtp.gtpc.v2.Impl.Gtp2HeaderImpl;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

public interface Gtp2Header extends GtpHeader {

    static Gtp2Header frame(final Buffer buffer) throws IllegalArgumentException, GtpParseException, GtpVersionException {
        assertNotNull(buffer, "The buffer cannot be null");
        return frame(buffer.toReadableBuffer());
    }

    static Gtp2Header frame(final ReadableBuffer buffer) throws IllegalArgumentException, GtpParseException, GtpVersionException {
        return Gtp2HeaderImpl.frame(buffer);
    }

    /**
     * The tunnel endpoint identifier is optional in GTPv2.
     */
    Optional<Teid> getTeid();

    @Override
    default int getVersion() {
        return 2;
    }

    @Override
    default Gtp2Header toGtp2Header() throws ClassCastException {
        return this;
    }


    /**
     * In GTPv2, the sequence no is mandatory, unlike GTPv1 where it is an optional parameter.
     */
    Buffer getSequenceNo();

    int getSequenceNoAsDecimal();
}
