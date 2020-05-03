package io.snice.codecs.codec.gtp.gtpc.v2.tliv.impl;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.TypeLengthInstanceValue;

/**
 * Base class for all fully framed/parsed GTPv2 Information Elements.
 */
public abstract class BaseTLIV implements TypeLengthInstanceValue {

    private final byte type;
    protected final Buffer value;

    protected BaseTLIV(final byte type, final Buffer value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public byte getType() {
        return type;
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
