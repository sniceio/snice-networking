package io.snice.networking.codec.diameter.avp.api;

import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.FramedAvp;

import io.snice.networking.codec.diameter.avp.impl.DiameterOctetStringAvp;
import io.snice.networking.codec.diameter.avp.type.OctetString;

/**
 * 
 */
public interface Msisdn extends Avp<OctetString> {

    int CODE = 701;

    @Override
    default long getCode() {
        return CODE;
    }

    default boolean isMsisdn() {
        return true;
    }

    default Msisdn toMsisdn() {
        return this;
    }

    static Msisdn parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + Msisdn.class.getName());
        }
        return new DefaultMsisdn(raw);
    }

    class DefaultMsisdn extends DiameterOctetStringAvp implements Msisdn {
        private DefaultMsisdn(final FramedAvp raw) {
            super(raw, true);
        }
    }
}
