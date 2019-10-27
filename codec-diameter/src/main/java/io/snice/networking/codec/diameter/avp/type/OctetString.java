package io.snice.networking.codec.diameter.avp.type;

import io.snice.buffer.Buffer;

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
    }
}
