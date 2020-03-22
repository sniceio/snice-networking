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

import io.snice.networking.codec.diameter.avp.impl.DiameterGroupedAvp;
import io.snice.networking.codec.diameter.avp.type.Grouped;

/**
 * This is an autogenerated class - do not edit
 * 
 */
public interface ExperimentalResult extends Avp<Grouped> {

    int CODE = 297;

    
    // TODO: grouped AVP should have some sort of 'of' method too
    

    @Override
    default long getCode() {
        return CODE;
    }

    default boolean isExperimentalResult() {
        return true;
    }

    default ExperimentalResult toExperimentalResult() {
        return this;
    }

    static ExperimentalResult parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + ExperimentalResult.class.getName());
        }
        return new DefaultExperimentalResult(raw);
    }

    class DefaultExperimentalResult extends DiameterGroupedAvp implements ExperimentalResult {
        private DefaultExperimentalResult(final FramedAvp raw) {
            super(raw);
        }

        @Override
        public ExperimentalResult ensure() {
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
                final ExperimentalResult o = (ExperimentalResult)other;
                final Grouped v = getValue();
                return v.equals(o.getValue());
            } catch (final ClassCastException e) {
                return false;
            }
        }
    }
}