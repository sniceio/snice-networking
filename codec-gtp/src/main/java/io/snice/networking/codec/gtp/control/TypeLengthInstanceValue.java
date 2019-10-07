package io.snice.networking.codec.gtp.control;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.networking.codec.gtp.control.impl.TypeLengthInstanceValueImpl;

/**
 * In GTPv2, all {@link InfoElement}s are of so-called TLIV - Type, Length, Instance, Value.
 */
public interface TypeLengthInstanceValue extends InfoElement {


    static TypeLengthInstanceValue frame(final Buffer buffer) {
        return TypeLengthInstanceValueImpl.frame(buffer);
    }

    static TypeLengthInstanceValue frame(final ReadableBuffer buffer) {
        return TypeLengthInstanceValueImpl.frame(buffer);
    }

    @Override
    default boolean isTypeLengthInstanceValue() {
        return true;
    }

    @Override
    default TypeLengthInstanceValue toTliv() throws ClassCastException {
        return this;
    }
}
