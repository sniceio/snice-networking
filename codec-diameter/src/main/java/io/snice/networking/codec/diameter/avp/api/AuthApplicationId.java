package io.snice.networking.codec.diameter.avp.api;

import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.FramedAvp;

import io.snice.networking.codec.diameter.avp.impl.DiameterUnsigned32Avp;
import io.snice.networking.codec.diameter.avp.type.Unsigned32;

/**
 * 
 */
public interface AuthApplicationId extends Avp<Unsigned32> {

    int CODE = 258;

    @Override
    default long getCode() {
        return CODE;
    }

    default boolean isAuthApplicationId() {
        return true;
    }

    default AuthApplicationId toAuthApplicationId() {
        return this;
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
