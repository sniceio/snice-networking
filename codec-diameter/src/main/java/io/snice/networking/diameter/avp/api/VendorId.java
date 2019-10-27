package io.snice.networking.diameter.avp.api;

import io.snice.networking.diameter.avp.Avp;
import io.snice.networking.diameter.avp.AvpParseException;
import io.snice.networking.diameter.avp.FramedAvp;
import io.snice.networking.diameter.avp.impl.DiameterUnsigned32Avp;
import io.snice.networking.diameter.avp.type.Unsigned32;

/**
 * 
 */
public interface VendorId extends Avp<Unsigned32> {

    int CODE = 266;

    @Override
    default long getCode() {
        return CODE;
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