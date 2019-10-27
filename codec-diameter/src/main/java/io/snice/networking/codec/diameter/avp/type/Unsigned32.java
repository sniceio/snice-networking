package io.snice.networking.codec.diameter.avp.type;

import io.snice.buffer.Buffer;

public interface Unsigned32 extends DiameterType {


    static Unsigned32 parse(final Buffer data) {
        return new DefaultUnsigned32(data.getUnsignedInt(0));
    }

    long getValue();

    class DefaultUnsigned32 implements Unsigned32 {
        private final long value;

        private DefaultUnsigned32(final long value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }
    }
}
