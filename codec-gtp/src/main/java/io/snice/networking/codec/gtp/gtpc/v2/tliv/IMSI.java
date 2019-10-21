package io.snice.networking.codec.gtp.gtpc.v2.tliv;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.gtp.gtpc.v2.Gtp2InfoElementType;

import static io.snice.preconditions.PreConditions.assertArgument;

public interface IMSI extends TypeLengthInstanceValue {

    Gtp2InfoElementType TYPE = Gtp2InfoElementType.IMSI;

    static IMSI frame(final Buffer value) {
        assertArgument(value != null && !value.isEmpty(), "The value of the IMSI cannot be null or empty");
        return null;
    }


    @Override
    default IMSI ensure() {
        return this;
    }

}
