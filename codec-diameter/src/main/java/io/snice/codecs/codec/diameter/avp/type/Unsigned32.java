package io.snice.codecs.codec.diameter.avp.type;

import io.snice.buffer.Buffer;
import io.snice.buffer.WritableBuffer;

import java.util.Objects;

public interface Unsigned32 extends DiameterType {


    static Unsigned32 parse(final Buffer data) {
        return new DefaultUnsigned32(data.getUnsignedInt(0));
    }

    static Unsigned32 of(final long value) {
        return new DefaultUnsigned32(value);
    }

    long getValue();

    @Override
    default int size() {
        return 4;
    }

    @Override
    default void writeValue(final WritableBuffer buffer) {
        buffer.write((int) getValue());
    }

    class DefaultUnsigned32 implements Unsigned32 {
        private final long value;

        private DefaultUnsigned32(final long value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final DefaultUnsigned32 that = (DefaultUnsigned32) o;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
