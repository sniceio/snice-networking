package io.snice.codecs.codec.gtp.gtpc.v2.tliv;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.codecs.codec.gtp.gtpc.InfoElement;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.impl.RawTypeLengthInstanceValue;

/**
 * In GTPv2, all {@link InfoElement}s are of so-called TLIV - Type, Length, Instance, Value.
 */
public interface TypeLengthInstanceValue extends InfoElement {

    String CANNOT_CAST_IE_OF_TYPE = "Cannot cast Info Element of type ";

    static TypeLengthInstanceValue frame(final Buffer buffer) {
        return RawTypeLengthInstanceValue.frame(buffer);
    }

    static TypeLengthInstanceValue frame(final ReadableBuffer buffer) {
        return RawTypeLengthInstanceValue.frame(buffer);
    }

    @Override
    default boolean isTypeLengthInstanceValue() {
        return true;
    }

    @Override
    default TypeLengthInstanceValue toTliv() throws ClassCastException {
        return this;
    }

    @Override
    TypeLengthInstanceValue ensure();

    default boolean isIMSI() {
        return getTypeAsDecimal() == 1;
    }

    default IMSI toIMSI() {
        throw new ClassCastException(CANNOT_CAST_IE_OF_TYPE + getClass().getName()
                + " to type " + IMSI.class.getName());
    }
}
