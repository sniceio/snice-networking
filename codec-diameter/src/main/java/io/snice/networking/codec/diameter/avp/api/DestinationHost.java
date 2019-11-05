package io.snice.networking.codec.diameter.avp.api;

import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.FramedAvp;

import io.snice.networking.codec.diameter.avp.impl.DiameterIdentityAvp;
import io.snice.networking.codec.diameter.avp.type.DiameterIdentity;

/**
 * 
 */
public interface DestinationHost extends Avp<DiameterIdentity> {

    int CODE = 293;

    @Override
    default long getCode() {
        return CODE;
    }

    default boolean isDestinationHost() {
        return true;
    }

    default DestinationHost toDestinationHost() {
        return this;
    }

    static DestinationHost parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + DestinationHost.class.getName());
        }
        return new DefaultDestinationHost(raw);
    }

    class DefaultDestinationHost extends DiameterIdentityAvp implements DestinationHost {
        private DefaultDestinationHost(final FramedAvp raw) {
            super(raw);
        }
    }
}
