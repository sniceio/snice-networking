package io.snice.networking.codec.diameter.avp.api;

import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.impl.DiameterIdentityAvp;
import io.snice.networking.codec.diameter.avp.type.DiameterIdentity;

/**
 * 
 */
public interface DestinationRealm extends Avp<DiameterIdentity> {

    int CODE = 283;

    @Override
    default long getCode() {
        return CODE;
    }

    default boolean isDestinationRealm() {
        return true;
    }

    default DestinationRealm toDestinationRealm() {
        return this;
    }

    static DestinationRealm parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to ensure the AVP into a " + DestinationRealm.class.getName());
        }
        return new DefaultDestinationRealm(raw);
    }

    class DefaultDestinationRealm extends DiameterIdentityAvp implements DestinationRealm {
        private DefaultDestinationRealm(final FramedAvp raw) {
            super(raw);
        }
    }
}
