package io.snice.networking.diameter.avp;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.networking.diameter.DiameterParseException;
import io.snice.networking.diameter.impl.DiameterParser;

/**
 * A {@link FramedAvp} is an AVP who has not been fully parsed, i.e., you only have access to the {@link AvpHeader}
 * and the raw data. However, if you want to convert it to a known type, you can call the {@link FramedAvp#parse()}
 * method to actually parse the structure into, hopefully, a known AVP.
 */
public interface FramedAvp {

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
     * Convenience method for getting the AVP code from the {@link AvpHeader}
     *
     * @return
     */
    default long getCode() {
        return getHeader().getCode();
    }

    Buffer getData();

    /**
     * Fully parse this raw AVP to something known. If the AVP isn't known,
     * then you'll get back a unknown AVP, which is really just the same as the
     * {@link FramedAvp} and then you have to figure things out for yourself.
     *
     * @return
     */
    Avp parse();
}