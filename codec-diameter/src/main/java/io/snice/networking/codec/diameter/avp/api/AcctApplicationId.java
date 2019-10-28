package io.snice.networking.codec.diameter.avp.api;

import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.FramedAvp;

import io.snice.networking.codec.diameter.avp.impl.DiameterUnsigned32Avp;
import io.snice.networking.codec.diameter.avp.type.Unsigned32;

/**
 * 
 */
public interface AcctApplicationId extends Avp<Unsigned32> {

    int CODE = 259;

    @Override
    default long getCode() {
        return CODE;
    }

    default boolean isAcctApplicationId() {
        return true;
    }

    default AcctApplicationId toAcctApplicationId() {
        return this;
    }

    static AcctApplicationId parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + AcctApplicationId.class.getName());
        }
        return new DefaultAcctApplicationId(raw);
    }

    class DefaultAcctApplicationId extends DiameterUnsigned32Avp implements AcctApplicationId {
        private DefaultAcctApplicationId(final FramedAvp raw) {
            super(raw);
        }
    }
}
