package io.snice.networking.codec.diameter.avp.api;

import io.snice.buffer.Buffer;
import io.snice.buffer.WritableBuffer;
import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.FramedAvp;

import io.snice.networking.codec.diameter.avp.impl.DiameterEnumeratedAvp;
import io.snice.networking.codec.diameter.avp.type.Enumerated;

import java.util.Optional;
import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * 
 */
public interface SubscriberStatus extends Avp<Enumerated<SubscriberStatus.Code>> {

    int CODE = 1424;
    
    SubscriberStatus ServiceGranted0 = SubscriberStatus.of(0);
    SubscriberStatus OperatorDeterminedBarring1 = SubscriberStatus.of(1);

    @Override
    default long getCode() {
        return CODE;
    }

    default SubscriberStatus toSubscriberStatus() {
        return this;
    }

    default boolean isSubscriberStatus() {
        return true;
    }

    @Override
    default void writeValue(final WritableBuffer buffer) {
        buffer.write(getValue().getValue());
    }

    static SubscriberStatus of(final int code) {
        final Optional<Code> c = Code.lookup(code);
        final EnumeratedHolder enumerated = new EnumeratedHolder(code, c);
        final Avp<Enumerated> avp = Avp.ofType(Enumerated.class).withValue(enumerated).withAvpCode(CODE).build();
        return new DefaultSubscriberStatus(avp, enumerated);
    }

    enum Code { 
        SERVICE_GRANTED_0("SERVICE_GRANTED", 0),
        OPERATOR_DETERMINED_BARRING_1("OPERATOR_DETERMINED_BARRING", 1);

        private final String name;
        private final int code;

        Code(final String name, final int code) {
            this.name = name;
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        static Optional<Code> lookup(final int code) {
            switch (code) { 
                case 0: return Optional.of(SERVICE_GRANTED_0);
                case 1: return Optional.of(OPERATOR_DETERMINED_BARRING_1);
                default:
                    return Optional.empty();
            }
        }
    }

    default Optional<Code> getAsEnum() {
        return getValue().getAsEnum();
    }

    static SubscriberStatus parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + SubscriberStatus.class.getName());
        }
        final Buffer data = raw.getData();
        final int value = data.getInt(0);
        final Optional<Code> e = Code.lookup(value);
        final EnumeratedHolder holder = new EnumeratedHolder(value, e);
        return new DefaultSubscriberStatus(raw, holder);
    }

    class DefaultSubscriberStatus extends DiameterEnumeratedAvp<Code> implements SubscriberStatus {
        private DefaultSubscriberStatus(final FramedAvp raw, final EnumeratedHolder value) {
            super(raw, value);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final DefaultSubscriberStatus that = (DefaultSubscriberStatus) o;
            return getValue().equals(that.getValue());
        }

        @Override
        public int hashCode() {
            return getValue().hashCode();
        }
    }

    /**
     * Ah! Must be a better way. I ran out of steam - getting late so it is what it is.
     */
    class EnumeratedHolder implements Enumerated<Code> {

        private final int code;
        private final Optional<Code> e;

        private EnumeratedHolder(final int code, final Optional<Code> e) {
            this.code = code;
            this.e = e;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final EnumeratedHolder that = (EnumeratedHolder) o;

            if (code != that.code) return false;
            return e.equals(that.e);
        }

        @Override
        public int hashCode() {
            int result = code;
            result = 31 * result + e.hashCode();
            return result;
        }

        @Override
        public Optional<Code> getAsEnum() {
            return e;
        }

        @Override
        public int getValue() {
            return code;
        }
    }

}
