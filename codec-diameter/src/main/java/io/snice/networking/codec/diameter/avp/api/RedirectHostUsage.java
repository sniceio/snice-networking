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
public interface RedirectHostUsage extends Avp<Enumerated<RedirectHostUsage.Code>> {

    int CODE = 261;
    
    RedirectHostUsage DontCare0 = RedirectHostUsage.of(0);
    RedirectHostUsage AllSession1 = RedirectHostUsage.of(1);
    RedirectHostUsage AllRealm2 = RedirectHostUsage.of(2);
    RedirectHostUsage RealmAndApplication3 = RedirectHostUsage.of(3);
    RedirectHostUsage AllApplication4 = RedirectHostUsage.of(4);
    RedirectHostUsage AllHost5 = RedirectHostUsage.of(5);
    RedirectHostUsage AllUser6 = RedirectHostUsage.of(6);

    @Override
    default long getCode() {
        return CODE;
    }

    default RedirectHostUsage toRedirectHostUsage() {
        return this;
    }

    default boolean isRedirectHostUsage() {
        return true;
    }

    @Override
    default void writeValue(final WritableBuffer buffer) {
        buffer.write(getValue().getValue());
    }

    static RedirectHostUsage of(final int code) {
        final Optional<Code> c = Code.lookup(code);
        final EnumeratedHolder enumerated = new EnumeratedHolder(code, c);
        final Avp<Enumerated> avp = Avp.ofType(Enumerated.class).withValue(enumerated).withAvpCode(CODE).build();
        return new DefaultRedirectHostUsage(avp, enumerated);
    }

    enum Code { 
        Dont_Care_0("Dont_Care", 0),
        All_Session_1("All_Session", 1),
        All_Realm_2("All_Realm", 2),
        Realm_and_Application_3("Realm_and_Application", 3),
        All_Application_4("All_Application", 4),
        All_Host_5("All_Host", 5),
        ALL_USER_6("ALL_USER", 6);

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
                case 0: return Optional.of(Dont_Care_0);
                case 1: return Optional.of(All_Session_1);
                case 2: return Optional.of(All_Realm_2);
                case 3: return Optional.of(Realm_and_Application_3);
                case 4: return Optional.of(All_Application_4);
                case 5: return Optional.of(All_Host_5);
                case 6: return Optional.of(ALL_USER_6);
                default:
                    return Optional.empty();
            }
        }
    }

    default Optional<Code> getAsEnum() {
        return getValue().getAsEnum();
    }

    static RedirectHostUsage parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to parse the AVP into a " + RedirectHostUsage.class.getName());
        }
        final Buffer data = raw.getData();
        final int value = data.getInt(0);
        final Optional<Code> e = Code.lookup(value);
        final EnumeratedHolder holder = new EnumeratedHolder(value, e);
        return new DefaultRedirectHostUsage(raw, holder);
    }

    class DefaultRedirectHostUsage extends DiameterEnumeratedAvp<Code> implements RedirectHostUsage {
        private DefaultRedirectHostUsage(final FramedAvp raw, final EnumeratedHolder value) {
            super(raw, value);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final DefaultRedirectHostUsage that = (DefaultRedirectHostUsage) o;
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
