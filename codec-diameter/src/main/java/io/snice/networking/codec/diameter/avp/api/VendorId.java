package io.snice.networking.codec.diameter.avp.api;

import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.FramedAvp;

import io.snice.networking.codec.diameter.avp.impl.DiameterUnsigned32Avp;
import io.snice.networking.codec.diameter.avp.type.Unsigned32;

/**
 * 
 */
public interface VendorId extends Avp<Unsigned32> {

    int CODE = 266;

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
    }
}
