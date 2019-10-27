package io.snice.networking.diameter.avp.api;

import io.snice.networking.diameter.avp.Avp;
import io.snice.networking.diameter.avp.AvpParseException;
import io.snice.networking.diameter.avp.FramedAvp;
import io.snice.networking.diameter.avp.impl.DiameterUnsigned32Avp;
import io.snice.networking.diameter.avp.type.Unsigned32;

/**
 * 
 */
public interface AuthApplicationId extends Avp<Unsigned32> {

    int CODE = 258;

    @Override
    default long getCode() {
        return CODE;
    }

    static AuthApplicationId parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + AuthApplicationId.class.getName());
        }
        return new DefaultAuthApplicationId(raw);
    }

    class DefaultAuthApplicationId extends DiameterUnsigned32Avp implements AuthApplicationId {
        private DefaultAuthApplicationId(final FramedAvp raw) {
            super(raw);
        }
    }
}