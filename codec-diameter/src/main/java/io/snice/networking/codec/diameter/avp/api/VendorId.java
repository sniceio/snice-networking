package io.snice.networking.codec.diameter.avp.api;


import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers; 
import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpMandatory;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.AvpProtected;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.Vendor;

import static io.snice.preconditions.PreConditions.assertNotNull;
import java.util.List;
import java.util.Optional;

import io.snice.networking.codec.diameter.avp.impl.DiameterUnsigned32Avp;
import io.snice.networking.codec.diameter.avp.type.Unsigned32;

/**
 * This is an autogenerated class - do not edit
 * 
 */
public interface VendorId extends Avp<Unsigned32> {

    int CODE = 266;

    
    static VendorId of(final Buffer value) {
        final Unsigned32 v = Unsigned32.parse(value);
        return of(v);
    }

    static VendorId of(final String value) {
        return of(Buffers.wrap(value));
    }

    
    static VendorId of(final long value) {
        final Unsigned32 v = Unsigned32.of(value);
        return of(v);
    }
    
    

    static VendorId of(final Unsigned32 value) {
        assertNotNull(value);
        final Builder<Unsigned32> builder =
                Avp.ofType(Unsigned32.class)
                        .withValue(value)
                        .withAvpCode(CODE)
                        .isMandatory(AvpMandatory.MUST.isMandatory())
                        .isProtected(AvpProtected.MAY.isProtected())
                        .withVendor(Vendor.NONE);

        return new DefaultVendorId(builder.build());
    }

    @Override
    default long getCode() {
        return CODE;
    }

    default boolean isVendorId() {
        return true;
    }

    default VendorId toVendorId() {
        return this;
    }

    static VendorId parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + VendorId.class.getName());
        }
        return new DefaultVendorId(raw);
    }

    class DefaultVendorId extends DiameterUnsigned32Avp implements VendorId {
        private DefaultVendorId(final FramedAvp raw) {
            super(raw);
        }

        @Override
        public VendorId ensure() {
            return this;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }

            if (other == null) {
                return false;
            }

            try {
                final VendorId o = (VendorId)((FramedAvp)other).ensure();
                final Unsigned32 v = getValue();
                return v.equals(o.getValue());
            } catch (final ClassCastException e) {
                return false;
            }
        }
    }
}
