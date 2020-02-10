package io.snice.networking.codec.diameter.avp.type;

import io.snice.buffer.Buffer;
import io.snice.buffer.WritableBuffer;

import java.util.Objects;

public interface IpAddress extends DiameterType {

    static IpAddress parse(final Buffer data) {
        return new DefaultIPAddress(data);
    }

    String asString();

    class DefaultIPAddress implements IpAddress {
        final Buffer value;

        private DefaultIPAddress(final Buffer value) {
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
            final DefaultIPAddress that = (DefaultIPAddress) o;
            return value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
