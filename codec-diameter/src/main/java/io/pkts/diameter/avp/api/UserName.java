package io.pkts.diameter.avp.api;

import io.pkts.diameter.avp.Avp;
import io.pkts.diameter.avp.AvpParseException;
import io.pkts.diameter.avp.FramedAvp;

import io.pkts.diameter.avp.impl.DiameterUtf8StringAvp;
import io.pkts.diameter.avp.type.UTF8String;

/**
 * 
 */
public interface UserName extends Avp<UTF8String> {

    int CODE = 1;

    @Override
    default long getCode() {
        return CODE;
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
