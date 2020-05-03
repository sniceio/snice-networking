package io.snice.codecs.codec.diameter.avp;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.buffer.WritableBuffer;
import io.snice.codecs.codec.diameter.DiameterParseException;
import io.snice.codecs.codec.diameter.avp.api.DestinationHost;
import io.snice.codecs.codec.diameter.avp.api.DestinationRealm;
import io.snice.codecs.codec.diameter.avp.api.ExperimentalResult;
import io.snice.codecs.codec.diameter.avp.api.ExperimentalResultCode;
import io.snice.codecs.codec.diameter.avp.api.HostIpAddress;
import io.snice.codecs.codec.diameter.avp.api.OriginHost;
import io.snice.codecs.codec.diameter.avp.api.OriginRealm;
import io.snice.codecs.codec.diameter.avp.api.ProductName;
import io.snice.codecs.codec.diameter.avp.api.ResultCode;
import io.snice.codecs.codec.diameter.avp.type.Enumerated;
import io.snice.codecs.codec.diameter.impl.DiameterParser;

/**
 * A {@link FramedAvp} is an AVP who has not been fully parsed, i.e., you only have access to the {@link AvpHeader}
 * and the raw data. However, if you want to convert it to a known type, you can call the {@link FramedAvp#ensure()}
 * method to actually ensure the structure into, hopefully, a known AVP.
 */
public interface FramedAvp {

    String CANNOT_CAST_AVP_OF_TYPE = "Cannot cast AVP of type ";

    static FramedAvp frame(final ReadableBuffer buffer) throws DiameterParseException {
        return DiameterParser.frameRawAvp(buffer);
    }

    /**
     * Return the amount of padding needed for this AVP.
     *
     * @return
     */
    int getPadding();

    AvpHeader getHeader();

    /**
     * Write the entire framed {@link Avp} to the given buffer.
     *
     * @param out
     */
    void writeTo(WritableBuffer out);

    /**
     * Get the total length of the AVP, including the header.
     * <p>
     * This is just a convenience method for <code>getHeader().getLength()</code>
     *
     * @return
     */
    default int getLength() {
        return getHeader().getLength();
    }


    /**
     * Convenience method for getting the AVP code from the {@link AvpHeader}
     *
     * @return
     */
    default long getCode() {
        return getHeader().getCode();
    }

    Buffer getData();

    /**
     * Fully ensure this raw AVP to something known. If the AVP isn't known,
     * then you'll get back a unknown AVP, which is really just the same as the
     * {@link FramedAvp} and then you have to figure things out for yourself.
     *
     * @return
     */
    Avp ensure();

    /**
     * Check if this AVP is an enumerated AVP.
     *
     * @return
     */
    default boolean isEnumerated() {
        return false;
    }

    default <E extends Enum<E>> Avp<Enumerated<E>> toEnumerated() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + this.getClass().getName() + " into a " + Enumerated.class.getName());
    }

    default boolean isOriginHost() {
        return false;
    }

    default OriginHost toOriginHost() {
        throw new ClassCastException(CANNOT_CAST_AVP_OF_TYPE + getClass().getName()
                + " to type " + OriginHost.class.getName());
    }

    default boolean isOriginRealm() {
        return false;
    }

    default OriginRealm toOriginRealm() {
        throw new ClassCastException(CANNOT_CAST_AVP_OF_TYPE + getClass().getName()
                + " to type " + OriginRealm.class.getName());
    }

    default DestinationRealm toDestinationRealm() {
        throw new ClassCastException(CANNOT_CAST_AVP_OF_TYPE + getClass().getName()
                + " to type " + DestinationRealm.class.getName());
    }


    default DestinationHost toDestinationHost() {
        throw new ClassCastException(CANNOT_CAST_AVP_OF_TYPE + getClass().getName()
                + " to type " + DestinationHost.class.getName());
    }

    default HostIpAddress toHostIpAddress() {
        throw new ClassCastException(CANNOT_CAST_AVP_OF_TYPE + getClass().getName()
                + " to type " + HostIpAddress.class.getName());
    }

    default ResultCode toResultCode() {
        throw new ClassCastException(CANNOT_CAST_AVP_OF_TYPE + getClass().getName()
                + " to type " + ResultCode.class.getName());
    }

    default boolean isResultCode() {
        return false;
    }

    default ExperimentalResultCode toExperimentalResultCode() {
        throw new ClassCastException(CANNOT_CAST_AVP_OF_TYPE + getClass().getName()
                + " to type " + ExperimentalResultCode.class.getName());
    }

    default boolean isExperimentalResultCode() {
        return false;
    }

    default ExperimentalResult toExperimentalResult() {
        throw new ClassCastException(CANNOT_CAST_AVP_OF_TYPE + getClass().getName()
                + " to type " + ExperimentalResult.class.getName());
    }

    default boolean isExperimentalResult() {
        return false;
    }

    default ProductName toProductName() {
        throw new ClassCastException(CANNOT_CAST_AVP_OF_TYPE + getClass().getName()
                + " to type " + ProductName.class.getName());
    }

    default boolean isProductName() {
        return false;
    }

}
