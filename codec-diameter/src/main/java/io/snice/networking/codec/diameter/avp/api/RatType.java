package io.snice.networking.codec.diameter.avp.api;

import io.snice.buffer.Buffer;
import io.snice.buffer.WritableBuffer;

import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpMandatory;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.AvpProtected;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.Vendor;

import io.snice.networking.codec.diameter.avp.impl.DiameterEnumeratedAvp;
import io.snice.networking.codec.diameter.avp.type.Enumerated;

import java.util.Optional;
import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * 
 */
public interface RatType extends Avp<Enumerated<RatType.Code>> {

    int CODE = 1032;
    
    RatType Wlan = RatType.of(0);
    RatType Virtual = RatType.of(1);
    RatType Utran = RatType.of(1000);
    RatType Geran = RatType.of(1001);
    RatType Gan = RatType.of(1002);
    RatType HspaEvolution = RatType.of(1003);
    RatType Eutran = RatType.of(1004);
    RatType EutranNbIot = RatType.of(1005);
    RatType Cdma20001x = RatType.of(2000);
    RatType Hrpd = RatType.of(2001);
    RatType Umb = RatType.of(2002);
    RatType Ehrpd = RatType.of(2003);

    @Override
    default long getCode() {
        return CODE;
    }

    default RatType toRatType() {
        return this;
    }

    default boolean isRatType() {
        return true;
    }

    @Override
    default void writeValue(final WritableBuffer buffer) {
        buffer.write(getValue().getValue());
    }

    static RatType of(final int code) {
        final Optional<Code> c = Code.lookup(code);
        final EnumeratedHolder enumerated = new EnumeratedHolder(code, c);
        final Avp<Enumerated> avp = Avp.ofType(Enumerated.class)
                .withValue(enumerated)
                .withAvpCode(CODE)
                .isMandatory(AvpMandatory.MUST_NOT.isMandatory())
                .isProtected(AvpProtected.MAY.isProtected())
                .withVendor(Vendor.TGPP)
                .build();
        return new DefaultRatType(avp, enumerated);
    }

    enum Code { 
        WLAN("WLAN", 0),
        VIRTUAL("VIRTUAL", 1),
        UTRAN("UTRAN", 1000),
        GERAN("GERAN", 1001),
        GAN("GAN", 1002),
        HSPA_EVOLUTION("HSPA_EVOLUTION", 1003),
        EUTRAN("EUTRAN", 1004),
        EUTRAN_NB_IoT("EUTRAN_NB_IoT", 1005),
        CDMA2000_1X("CDMA2000_1X", 2000),
        HRPD("HRPD", 2001),
        UMB("UMB", 2002),
        EHRPD("EHRPD", 2003);

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
                case 0: return Optional.of(WLAN);
                case 1: return Optional.of(VIRTUAL);
                case 1000: return Optional.of(UTRAN);
                case 1001: return Optional.of(GERAN);
                case 1002: return Optional.of(GAN);
                case 1003: return Optional.of(HSPA_EVOLUTION);
                case 1004: return Optional.of(EUTRAN);
                case 1005: return Optional.of(EUTRAN_NB_IoT);
                case 2000: return Optional.of(CDMA2000_1X);
                case 2001: return Optional.of(HRPD);
                case 2002: return Optional.of(UMB);
                case 2003: return Optional.of(EHRPD);
                default:
                    return Optional.empty();
            }
        }
    }

    default Optional<Code> getAsEnum() {
        return getValue().getAsEnum();
    }

    static RatType parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + RatType.class.getName());
        }
        final Buffer data = raw.getData();
        final int value = data.getInt(0);
        final Optional<Code> e = Code.lookup(value);
        final EnumeratedHolder holder = new EnumeratedHolder(value, e);
        return new DefaultRatType(raw, holder);
    }

    class DefaultRatType extends DiameterEnumeratedAvp<Code> implements RatType {
        private DefaultRatType(final FramedAvp raw, final EnumeratedHolder value) {
            super(raw, value);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final DefaultRatType that = (DefaultRatType) o;
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