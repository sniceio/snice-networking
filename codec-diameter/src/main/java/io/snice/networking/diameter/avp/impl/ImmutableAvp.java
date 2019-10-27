package io.snice.networking.diameter.avp.impl;

import io.snice.buffer.Buffer;
import io.snice.networking.diameter.avp.Avp;
import io.snice.networking.diameter.avp.AvpHeader;
import io.snice.networking.diameter.avp.FramedAvp;
import io.snice.networking.diameter.avp.type.DiameterType;

public class ImmutableAvp<T extends DiameterType> implements Avp<T> {

    private final FramedAvp raw;
    private final T value;

    public ImmutableAvp(final FramedAvp raw, final T value) {
        this.raw = raw;
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public int getPadding() {
        return raw.getPadding();
    }

    @Override
    public AvpHeader getHeader() {
        return raw.getHeader();
    }

    @Override
    public Buffer getData() {
        return raw.getData();
    }

    @Override
    public Avp<T> parse() {
        return this;
    }
}
