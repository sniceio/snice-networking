package io.snice.codecs.codec.diameter.avp.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.WritableBuffer;
import io.snice.codecs.codec.diameter.avp.Avp;
import io.snice.codecs.codec.diameter.avp.AvpHeader;
import io.snice.codecs.codec.diameter.avp.FramedAvp;
import io.snice.codecs.codec.diameter.avp.type.DiameterType;

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
    public void writeTo(final WritableBuffer out) {
        raw.writeTo(out);
    }

    @Override
    public Buffer getData() {
        return raw.getData();
    }

    @Override
    public Avp ensure() {
        return raw.ensure();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AVP [");
        sb.append(raw.getHeader().toString());
        sb.append(" ");
        sb.append(value);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ImmutableAvp<?> that = (ImmutableAvp<?>) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
