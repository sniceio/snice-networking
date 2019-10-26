package io.pkts.diameter.avp.type;

import io.snice.buffer.Buffer;

public interface Integer32 extends DiameterType {

    static Integer32 parse(final Buffer data) {
        return new DefaultInteger32(data.getInt(0));
    }

    int getValue();

    class DefaultInteger32 implements Integer32 {
        private final int value;

        private DefaultInteger32(final int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }
}
