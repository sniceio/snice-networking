package io.snice.networking.codec.diameter.avp.api;

import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.FramedAvp;

import io.snice.networking.codec.diameter.avp.impl.DiameterUtf8StringAvp;
import io.snice.networking.codec.diameter.avp.type.UTF8String;

/**
 * 
 */
public interface SessionId extends Avp<UTF8String> {

    int CODE = 263;

    @Override
    default long getCode() {
        return CODE;
    }

    default boolean isSessionId() {
        return true;
    }

    default SessionId toSessionId() {
        return this;
    }

    static SessionId parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + SessionId.class.getName());
        }
        return new DefaultSessionId(raw);
    }

    class DefaultSessionId extends DiameterUtf8StringAvp implements SessionId {
        private DefaultSessionId(final FramedAvp raw) {
            super(raw);
        }
    }
}
