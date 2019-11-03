package io.snice.networking.codec.gtp.gtpc.v2.tliv;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.gtp.gtpc.v2.Gtp2InfoElementType;
import io.snice.networking.codec.gtp.gtpc.v2.tliv.impl.IMSIImpl;

public interface IMSI extends TypeLengthInstanceValue {

    Gtp2InfoElementType TYPE = Gtp2InfoElementType.IMSI;

    static IMSI frame(final Buffer value) {
        return IMSIImpl.frame(value);
    }

    @Override
    default IMSI toIMSI() {
        return this;
    }

    @Override
    default IMSI ensure() {
        return this;
    }

}
