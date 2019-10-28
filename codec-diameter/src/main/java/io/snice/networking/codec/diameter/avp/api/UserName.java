package io.snice.networking.codec.diameter.avp.api;

import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.FramedAvp;

import io.snice.networking.codec.diameter.avp.impl.DiameterUtf8StringAvp;
import io.snice.networking.codec.diameter.avp.type.UTF8String;

/**
 * 
 */
public interface UserName extends Avp<UTF8String> {

    int CODE = 1;

    @Override
    default long getCode() {
        return CODE;
    }

    default boolean isUserName() {
        return true;
    }

    default UserName toUserName() {
        return this;
    }

    static UserName parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + UserName.class.getName());
        }
        return new DefaultUserName(raw);
    }

    class DefaultUserName extends DiameterUtf8StringAvp implements UserName {
        private DefaultUserName(final FramedAvp raw) {
            super(raw);
        }
    }
}
