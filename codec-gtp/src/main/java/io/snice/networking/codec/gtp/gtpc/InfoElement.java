package io.snice.networking.codec.gtp.gtpc;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.gtp.gtpc.v2.tliv.TypeLengthInstanceValue;

public interface InfoElement {

    byte getType();

    default int getTypeAsDecimal() {
        return Byte.toUnsignedInt(getType());
    }

    int getLength();

    Buffer getValue();

    /**
     * As most things in this library are done lazily, such as framing info elements (IE), You can make sure
     * that a particular IE has indeed been parsed to the more specific IE type by calling
     * this method. If the IE has yet not been parsed fully, e.g., we may have extracted out a
     * {@link } but it is still in its "raw" form and therefore represented as a
     * {@link TypeLengthInstanceValue} as opposed to an actual {@link ContactHeader} but by calling this method
     * you will force the library to actually fully frame it.
     * <p>
     * Note, if the header is successfully parsed into a more explicit header type you may
     * still not really know what to cast it so in order to make life somewhat easier you can
     * use the isXxxxIE methods (such as {@link SipHeader#isAddressParametersHeader()} to
     * check what type it possible can be and then use the corresponding toXxxxxIE to
     * "cast" it.
     *
     * @return
     */
    <T extends InfoElement> T ensure();

    default boolean isTypeLengthInstanceValue() {
        return false;
    }

    default TypeLengthInstanceValue toTliv() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + getClass().getName() + " into a " + TypeLengthInstanceValue.class.getName());
    }
}
