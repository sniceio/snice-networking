package io.snice.networking.codec.diameter.avp.api;

import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.impl.DiameterIdentityAvp;
import io.snice.networking.codec.diameter.avp.type.DiameterIdentity;

/**
 * 
 */
public interface OriginHost extends Avp<DiameterIdentity> {

    int CODE = 264;

    @Override
    default long getCode() {
        return CODE;
    }

    @Override
    default boolean isOriginHost() {
        return true;
    }

    @Override
    default OriginHost toOriginHost() {
        return this;
    }

    static OriginHost parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + OriginHost.class.getName());
        }
        return new DefaultOriginHost(raw);
    }

    class DefaultOriginHost extends DiameterIdentityAvp implements OriginHost {
        private DefaultOriginHost(final FramedAvp raw) {
            super(raw);
        }
    }
}
