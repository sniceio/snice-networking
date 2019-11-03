package io.snice.networking.codec.diameter.avp.api;

import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.impl.DiameterGroupedAvp;
import io.snice.networking.codec.diameter.avp.type.Grouped;

/**
 * 
 */
public interface VendorSpecificApplicationId extends Avp<Grouped> {

    int CODE = 260;

    @Override
    default long getCode() {
        return CODE;
    }

    default boolean isVendorSpecificApplicationId() {
        return true;
    }

    default VendorSpecificApplicationId toVendorSpecificApplicationId() {
        return this;
    }

    static VendorSpecificApplicationId parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to ensure the AVP into a " + VendorSpecificApplicationId.class.getName());
        }
        return new DefaultVendorSpecificApplicationId(raw);
    }

    class DefaultVendorSpecificApplicationId extends DiameterGroupedAvp implements VendorSpecificApplicationId {
        private DefaultVendorSpecificApplicationId(final FramedAvp raw) {
            super(raw);
        }
    }
}
