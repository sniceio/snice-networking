package io.snice.networking.codec.diameter.avp.type;

import io.snice.buffer.Buffer;

import java.util.Objects;

public interface OctetString extends DiameterType {


    static OctetString parse(final Buffer data) {
        // TODO: this is not really correct. Will fix later.
        return new DefaultOctetString(data);
    }

    String getValue();

    class DefaultOctetString implements OctetString {
        private final Buffer value;

        private DefaultOctetString(final Buffer value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value.toString();
        }

        @Override
        public int size() {
            System.err.println("Size of OctetString: " + value.capacity());
            return value.capacity();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final DefaultOctetString that = (DefaultOctetString) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
