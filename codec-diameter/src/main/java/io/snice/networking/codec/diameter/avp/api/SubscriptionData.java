package io.snice.networking.codec.diameter.avp.api;

import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.FramedAvp;

import io.snice.networking.codec.diameter.avp.impl.DiameterGroupedAvp;
import io.snice.networking.codec.diameter.avp.type.Grouped;

/**
 * 
 */
public interface SubscriptionData extends Avp<Grouped> {

    int CODE = 1400;

    @Override
    default long getCode() {
        return CODE;
    }

    default boolean isSubscriptionData() {
        return true;
    }

    default SubscriptionData toSubscriptionData() {
        return this;
    }

    static SubscriptionData parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + SubscriptionData.class.getName());
        }
        return new DefaultSubscriptionData(raw);
    }

    class DefaultSubscriptionData extends DiameterGroupedAvp implements SubscriptionData {
        private DefaultSubscriptionData(final FramedAvp raw) {
            super(raw);
        }
    }
}
