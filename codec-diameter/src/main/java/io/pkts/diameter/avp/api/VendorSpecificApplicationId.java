package io.pkts.diameter.avp.api;

import io.pkts.diameter.avp.Avp;
import io.pkts.diameter.avp.AvpParseException;
import io.pkts.diameter.avp.FramedAvp;

import io.pkts.diameter.avp.impl.DiameterGroupedAvp;
import io.pkts.diameter.avp.type.Grouped;

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
