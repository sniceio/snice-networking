package io.snice.networking.codec.diameter.avp.api;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpParseException;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.impl.DiameterEnumeratedAvp;
import io.snice.networking.codec.diameter.avp.type.Enumerated;

import java.util.Optional;

/**
 * 
 nisse
 */
public interface RedirectHostUsage extends Avp<Enumerated<RedirectHostUsage.RedirectHostUsageEnum>> {

    int CODE = 261;

    @Override
    default long getCode() {
        return CODE;
    }

    enum RedirectHostUsageEnum { 
        Dont_Care_0("Dont_Care", 0),
        All_Session_1("All_Session", 1),
        All_Realm_2("All_Realm", 2),
        Realm_and_Application_3("Realm_and_Application", 3),
        All_Application_4("All_Application", 4),
        All_Host_5("All_Host", 5),
        ALL_USER_6("ALL_USER", 6);

        private final String name;
        private final int code;

        RedirectHostUsageEnum(final String name, final int code) {
            this.name = name;
            this.code = code;
        }

        static Optional<RedirectHostUsageEnum> lookup(final int code) {
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

    default Optional<RedirectHostUsageEnum> getAsEnum() {
        return getValue().getAsEnum();
    }

    static RedirectHostUsage parse(final FramedAvp raw) {
        if (CODE != raw.getCode()) {
            throw new AvpParseException("AVP Code mismatch - unable to ensure the AVP into a " + RedirectHostUsage.class.getName());
        }
        final Buffer data = raw.getData();
        final int value = data.getInt(0);
        final Optional<RedirectHostUsageEnum> e = RedirectHostUsageEnum.lookup(value);
        final EnumeratedHolder holder = new EnumeratedHolder(value, e);
        return new DefaultRedirectHostUsage(raw, holder);
    }

    class DefaultRedirectHostUsage extends DiameterEnumeratedAvp<RedirectHostUsageEnum> implements RedirectHostUsage {
        private DefaultRedirectHostUsage(final FramedAvp raw, final EnumeratedHolder value) {
            super(raw, value);
        }
    }

    /**
     * Ah! Must be a better way. I ran out of steam - getting late so it is what it is.
     */
    class EnumeratedHolder implements Enumerated<RedirectHostUsageEnum> {

        private final int code;
        private final Optional<RedirectHostUsageEnum> e;

        private EnumeratedHolder(final int code, final Optional<RedirectHostUsageEnum> e) {
            this.code = code;
            this.e = e;
        }

        @Override
        public Optional<RedirectHostUsageEnum> getAsEnum() {
            return e;
        }

        @Override
        public int getValue() {
            return code;
        }
    }

}
