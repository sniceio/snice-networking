package io.snice.networking.codec.diameter.avp.type;

import io.snice.buffer.Buffer;

public interface UTF8String extends DiameterType {


    static UTF8String parse(final Buffer data) {
        return new DefaultUTF8String(data.toUTF8String());
    }

    String getValue();

    class DefaultUTF8String implements UTF8String {
        private final String value;

        private DefaultUTF8String(final String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }
}
