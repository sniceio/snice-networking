package io.snice.codecs.codec.diameter.avp;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.buffer.WritableBuffer;
import io.snice.codecs.codec.diameter.DiameterParseException;
import io.snice.codecs.codec.diameter.avp.impl.ImmutableAvpHeader;
import io.snice.codecs.codec.diameter.impl.DiameterParser;

import java.util.Optional;

/**
 * The {@link AvpHeader} contains the AVP code, length, flags and potentially
 * the vendor specific ID.
 *
 * @author jonas@jonasborjesson.com
 */
public interface AvpHeader {

    static AvpHeader frame(final ReadableBuffer buffer) throws DiameterParseException {
        return DiameterParser.frameAvpHeader(buffer);
    }

    static Builder withCode(final long code) {
        return ImmutableAvpHeader.withCode(code);
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

    /**
     * Get the raw {@link Buffer} of this {@link AvpHeader}.
     */
    Buffer getBuffer();

    void writeTo(WritableBuffer out);

    interface Builder {

        /**
         * Set the 'M' bit, which indicates that this {@link Avp} is mandatory.
         * <p>
         * Default value is false.
         */
        Builder isMandatory();

        /**
         * Set the 'P' big, which indicates that this {@link Avp} is protected.
         */
        Builder isProtected();

        /**
         * Set the optional vendor id. If set, the 'V' bit will also
         * be set, indicating that this {@link AvpHeader} has the vendor id
         * set.
         *
         * @param vendorId
         */
        Builder withVendorId(long vendorId);

        AvpHeader build();
    }
}
