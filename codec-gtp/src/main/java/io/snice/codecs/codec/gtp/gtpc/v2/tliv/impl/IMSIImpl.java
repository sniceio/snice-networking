package io.snice.codecs.codec.gtp.gtpc.v2.tliv.impl;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.IMSI;

import static io.snice.preconditions.PreConditions.assertArgument;

public class IMSIImpl extends BaseTLIV implements IMSI {

    public static IMSI frame(final Buffer value) {
        assertArgument(value != null && !value.isEmpty(), "The value of the IMSI cannot be null or empty");
        return new IMSIImpl(value);
    }

    private IMSIImpl(final Buffer value) {
        super(IMSI.TYPE.getType(), value);
    }

    @Override
    public String toString() {
        return value.toTBCD();
    }

}
