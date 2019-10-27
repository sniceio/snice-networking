package io.snice.networking.diameter.avp.api;

import io.snice.networking.diameter.avp.Avp;
import io.snice.networking.diameter.avp.AvpParseException;
import io.snice.networking.diameter.avp.FramedAvp;
import io.snice.networking.diameter.avp.impl.DiameterGroupedAvp;
import io.snice.networking.diameter.avp.type.Grouped;

/**
 * 
 */
public interface VendorSpecificApplicationId extends Avp<Grouped> {

    int CODE = 260;

    @Override
    default long getCode() {
        return CODE;
    }

    static VendorSpecificApplicationId parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + VendorSpecificApplicationId.class.getName());
        }
        return new DefaultVendorSpecificApplicationId(raw);
    }

    class DefaultVendorSpecificApplicationId extends DiameterGroupedAvp implements VendorSpecificApplicationId {
        private DefaultVendorSpecificApplicationId(final FramedAvp raw) {
            super(raw);
        }
    }
}
