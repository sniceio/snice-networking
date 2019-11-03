package io.snice.networking.codec.diameter.avp.api;

import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.impl.DiameterIdentityAvp;
import io.snice.networking.codec.diameter.avp.type.DiameterIdentity;

/**
 * 
 */
public interface OriginRealm extends Avp<DiameterIdentity> {

    int CODE = 296;

    @Override
    default long getCode() {
        return CODE;
    }

    @Override
    default boolean isOriginRealm() {
        return true;
    }

    @Override
    default OriginRealm toOriginRealm() {
        return this;
    }

    static OriginRealm parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to ensure the AVP into a " + OriginRealm.class.getName());
        }
        return new DefaultOriginRealm(raw);
    }

    class DefaultOriginRealm extends DiameterIdentityAvp implements OriginRealm {
        private DefaultOriginRealm(final FramedAvp raw) {
            super(raw);
        }
    }
}
