package io.snice.codecs.codec.diameter.avp.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.WritableBuffer;
import io.snice.codecs.codec.diameter.avp.Avp;
import io.snice.codecs.codec.diameter.avp.AvpHeader;
import io.snice.codecs.codec.diameter.avp.FramedAvp;
import io.snice.codecs.codec.diameter.impl.DiameterParser;

public class ImmutableFramedAvp implements FramedAvp {

    private final AvpHeader header;
    private final Buffer data;


    public ImmutableFramedAvp(final AvpHeader header, final Buffer data) {
        this.header = header;
        this.data = data;
    }

    /**
     * From RFC 6733:
     * <p>
     * Each AVP of type OctetString MUST be padded to align on a 32-bit
     * boundary, while other AVP types align naturally.  A number of zero-
     * valued bytes are added to the end of the AVP Data field until a word
     * boundary is reached.  The length of the padding is not reflected in
     * the AVP Length field.
     *
     * @return
     */
    @Override
    public int getPadding() {
        final int padding = header.getLength() % 4;
        if (padding != 0) {
            return 4 - padding;
        }
        return 0;
    }

    @Override
    public String toString() {
        return header.toString();
    }

    @Override
    public AvpHeader getHeader() {
        return header;
    }

    @Override
    public void writeTo(final WritableBuffer out) {
        header.writeTo(out);
        data.writeTo(out);

        switch (getPadding()) {
            case 3:
                out.write((byte) 0);
            case 2:
                out.write((byte) 0);
            case 1:
                out.write((byte) 0);
            default:
        }
    }

    @Override
    public Buffer getData() {
        // must slice so that the returned data has it's own reader index etc.
        return data.slice();
    }

    @Override
    public Avp ensure() {
        return DiameterParser.parseAvp(this);
    }
}
