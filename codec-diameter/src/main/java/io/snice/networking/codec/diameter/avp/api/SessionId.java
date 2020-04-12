package io.snice.networking.codec.diameter.avp.api;


import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers; 
import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpMandatory;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.AvpProtected;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.Vendor;

import static io.snice.preconditions.PreConditions.assertNotNull;
import java.util.List;
import java.util.Optional;

import io.snice.networking.codec.diameter.avp.impl.DiameterUtf8StringAvp;
import io.snice.networking.codec.diameter.avp.type.UTF8String;

/**
 * This is an autogenerated class - do not edit
 * 
 */
public interface SessionId extends Avp<UTF8String> {

    int CODE = 263;

    
    static SessionId of(final Buffer value) {
        final UTF8String v = UTF8String.parse(value);
        return of(v);
    }

    static SessionId of(final String value) {
        return of(Buffers.wrap(value));
    }

    
    

    static SessionId of(final UTF8String value) {
        assertNotNull(value);
        final Builder<UTF8String> builder =
                Avp.ofType(UTF8String.class)
                        .withValue(value)
                        .withAvpCode(CODE)
                        .isMandatory(AvpMandatory.MUST.isMandatory())
                        .isProtected(AvpProtected.MUST_NOT.isProtected())
                        .withVendor(Vendor.NONE);

        return new DefaultSessionId(builder.build());
    }

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

        @Override
        public SessionId ensure() {
            return this;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }

            if (other == null) {
                return false;
            }

            try {
                final SessionId o = (SessionId)((FramedAvp)other).ensure();
                final UTF8String v = getValue();
                return v.equals(o.getValue());
            } catch (final ClassCastException e) {
                return false;
            }
        }
    }
}
