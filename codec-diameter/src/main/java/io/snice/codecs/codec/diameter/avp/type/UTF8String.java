package io.snice.codecs.codec.diameter.avp.type;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.buffer.WritableBuffer;

import java.util.Objects;

public interface UTF8String extends DiameterType {


    static UTF8String parse(final Buffer data) {
        return new DefaultUTF8String(data);
    }

    static UTF8String of(final String value) {
        final Buffer raw = Buffers.wrap(value);
        return new DefaultUTF8String(raw, value);
    }

    String getValue();

    class DefaultUTF8String implements UTF8String {
        private final Buffer raw;
        private final String value;

        private DefaultUTF8String(final Buffer raw) {
            this(raw, raw.toUTF8String());
        }

        private DefaultUTF8String(final Buffer raw, final String value) {
            this.raw = raw;
            this.value = value;
        }

        @Override
        public void writeValue(final WritableBuffer buffer) {
            raw.writeTo(buffer);
        }

        @Override
        public int size() {
            return raw.capacity();
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final DefaultUTF8String that = (DefaultUTF8String) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
