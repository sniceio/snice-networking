package io.snice.networking.codec.diameter.avp.type;

import io.snice.buffer.Buffer;

public interface DiameterIdentity extends DiameterType {

    static DiameterIdentity parse(final Buffer data) {
        return new DefaultDiameterIdentity(data);
    }

    String asString();

    class DefaultDiameterIdentity implements DiameterIdentity {
        final Buffer value;

        private DefaultDiameterIdentity(final Buffer value) {
            this.value = value;
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
    }
}
