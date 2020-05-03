package io.snice.codecs.codec.diameter.avp.type;

import io.snice.buffer.Buffer;
import io.snice.buffer.WritableBuffer;

import java.util.Objects;

import static io.snice.preconditions.PreConditions.assertArgument;

public interface DiameterIdentity extends DiameterType {

    static DiameterIdentity parse(final Buffer data) {
        assertArgument(data != null && !data.isEmpty());
        return new DefaultDiameterIdentity(data);
    }

    String asString();

    class DefaultDiameterIdentity implements DiameterIdentity {
        final Buffer value;

        private DefaultDiameterIdentity(final Buffer value) {
            this.value = value;
        }

        @Override
        public void writeValue(final WritableBuffer buffer) {
            buffer.write(value);
        }

        @Override
        public String toString() {
            return value.toString();
        }

        @Override
        public String asString() {
            return value.toString();
        }

        @Override
        public int size() {
            return value.capacity();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final DefaultDiameterIdentity that = (DefaultDiameterIdentity) o;
            return value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
