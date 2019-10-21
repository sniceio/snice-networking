package io.snice.networking.codec.gtp.gtpc.v2.tliv.impl;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.gtp.gtpc.v2.tliv.TypeLengthInstanceValue;

/**
 * Base class for all fully framed/parsed GTPv2 Information Elements.
 */
public abstract class BaseTLIV implements TypeLengthInstanceValue {

    // protected BaseTLIV()

    @Override
    public byte getType() {
        return 0;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public Buffer getValue() {
        return null;
    }
}
