package io.snice.networking.codec.gtp.gtpc.v2.tliv.impl;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.gtp.gtpc.v2.tliv.IMSI;

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
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.capacity(); ++i) {
            final var b = value.getByte(i);
            final int i1 = (b & 0xF0) >> 4;
            final int i2 = b & 0x0F;
            sb.append(i2);
            sb.append(i1);
        }

        return sb.toString();
    }

}
