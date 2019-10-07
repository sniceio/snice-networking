package io.snice.networking.codec.gtp.control;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.networking.codec.gtp.GtpHeader;
import io.snice.networking.codec.gtp.GtpParseException;
import io.snice.networking.codec.gtp.GtpVersionException;
import io.snice.networking.codec.gtp.Teid;
import io.snice.networking.codec.gtp.control.impl.Gtp1HeaderImpl;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

public interface Gtp1Header extends GtpHeader {

    static Gtp1Header frame(final Buffer buffer) throws IllegalArgumentException, GtpParseException, GtpVersionException {
        assertNotNull(buffer, "The buffer cannot be null");
        return frame(buffer.toReadableBuffer());
    }

    static Gtp1Header frame(final ReadableBuffer buffer) throws IllegalArgumentException, GtpParseException, GtpVersionException {
        return Gtp1HeaderImpl.frame(buffer);
    }

    Teid getTeid();

    @Override
    default int getVersion() {
        return 1;
    }

    @Override
    default Gtp1Header toGtp1Header() throws ClassCastException {
        return this;
    }

    /**
     * In GTPv1, the sequence no is an optional parameter, whereas in GTPv2 it is a mandatory
     * parameter and also 1 byte longer.
     */
    Optional<Buffer> getSequenceNo();

    Optional<Integer> getSequenceNoAsDecimal();
}
