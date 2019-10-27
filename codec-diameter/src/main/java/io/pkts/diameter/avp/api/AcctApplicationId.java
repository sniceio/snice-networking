package io.pkts.diameter.avp.api;

import io.pkts.diameter.avp.Avp;
import io.pkts.diameter.avp.AvpParseException;
import io.pkts.diameter.avp.FramedAvp;

import io.pkts.diameter.avp.impl.DiameterUnsigned32Avp;
import io.pkts.diameter.avp.type.Unsigned32;

/**
 * 
 */
public interface AcctApplicationId extends Avp<Unsigned32> {

    int CODE = 259;

    @Override
    default long getCode() {
        return CODE;
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
