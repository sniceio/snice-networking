package io.snice.networking.codec.diameter.avp.api;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpMandatory;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.AvpProtected;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.Vendor;

import io.snice.networking.codec.diameter.avp.impl.DiameterUnsigned32Avp;
import io.snice.networking.codec.diameter.avp.type.Unsigned32;

/**
 * This is an autogenerated class - do not edit
 * 
 */
public interface AuthApplicationId extends Avp<Unsigned32> {

    int CODE = 258;

    
    static AuthApplicationId of(final Buffer value) {
        final Unsigned32 v = Unsigned32.parse(value);
        final Builder<Unsigned32> builder =
                Avp.ofType(Unsigned32.class)
                        .withValue(v)
                        .withAvpCode(CODE)
                        .isMandatory(AvpMandatory.MUST.isMandatory())
                        .isProtected(AvpProtected.MUST_NOT.isProtected())
                        .withVendor(Vendor.NONE);

        return new DefaultAuthApplicationId(builder.build());
    }

    static AuthApplicationId of(final String value) {
        return of(Buffers.wrap(value));
    }
    

    @Override
    default long getCode() {
        return CODE;
    }

    default boolean isAuthApplicationId() {
        return true;
    }

    default AuthApplicationId toAuthApplicationId() {
        return this;
    }

    static AuthApplicationId parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + AuthApplicationId.class.getName());
        }
        return new DefaultAuthApplicationId(raw);
    }

    class DefaultAuthApplicationId extends DiameterUnsigned32Avp implements AuthApplicationId {
        private DefaultAuthApplicationId(final FramedAvp raw) {
            super(raw);
        }
    }
}
