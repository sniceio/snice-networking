package io.snice.networking.codec.diameter.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.WritableBuffer;
import io.snice.networking.codec.diameter.DiameterHeader;

/**
 *
 * @author jonas@jonasborjesson.com
 */
public class ImmutableDiameterHeader implements DiameterHeader {
    private final Buffer buffer;

    public static DiameterHeader.Builder of() {
        final WritableBuffer buffer = WritableBuffer.of(20);
        buffer.fastForwardWriterIndex();
        return new ImmutableDiameterHeader.Builder(buffer);
    }

    protected ImmutableDiameterHeader(final Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int getLength() {
        return buffer.getIntFromThreeOctets(1);
    }

    @Override
    public int getVersion() {
        return buffer.getByte(0);
    }

    @Override
    public boolean isRequest() {
        // 5th byte is the command flags and the 7th bit is whether this
        // is a request or answer.
        return buffer.getBit7(4);
    }

    @Override
    public boolean isProxiable() {
        return buffer.getBit6(4);
    }

    @Override
    public boolean isError() {
        return buffer.getBit5(4);
    }

    @Override
    public boolean isPossiblyRetransmission() {
        return buffer.getBit4(4);
    }

    @Override
    public int getCommandCode() {
        return buffer.getIntFromThreeOctets(5);
    }

    @Override
    public long getApplicationId() {
        return buffer.getUnsignedInt(8);
    }

    @Override
    public long getHopByHopId() {
        return buffer.getUnsignedInt(12);
    }

    @Override
    public long getEndToEndId() {
        return buffer.getUnsignedInt(16);
    }

    @Override
    public Buffer getBuffer() {
        return buffer;
    }

    @Override
    public DiameterHeader.Builder copy() {
        // remember that a buffer.toWritableBuffer is forced to copy the entire underlying
        // byte-array since the default buffer is immutable and as such, in order to guarantee
        // this property, it has to copy it in order to turn it into a WritableBuffer
        final WritableBuffer copy = buffer.toWritableBuffer();
        return new Builder(copy);
    }

    private final byte getByte(final int index) {
            return buffer.getByte(index);
    }

    @Override
    public boolean validate() {
        // the version must be 1 so if it isn't, bail out.
        if (!buffer.getBit0(0)) {
            return false;
        }

        // then, in the 5th byte we have the Command Flags. Currently, the last 4 bits of that
        // byte is always NOT set. These are reserved bits which currently isn't being used so
        // expect them to indeed be zero...
        // Perhaps this check is dangerous in that if one of the bits is being used, we won't accept the
        // message even though perhaps it really doesn't matter.
        if (!((getByte(4) & 0b00001111) == 0b00000000)) {
            return false;
        }

        // also, the length must be a multiple of 4 according to spec since the AVPs will be padded
        // if need be.
        return getLength() % 4 == 0;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(isRequest() ? "Request" : "Response");
        sb.append(", Length: ").append(getLength());
        sb.append(", Flags: ");
        sb.append(isRequest() ? "R" : "r");
        sb.append(isProxiable() ? "P" : "p");
        sb.append(isError() ? "E" : "e");
        sb.append(isPossiblyRetransmission() ? "T" : "t");
        sb.append("0000");
        sb.append(", Cmd Code: ").append(getCommandCode());
        sb.append(", App Id: ").append(getApplicationId());
        return sb.toString();
    }

    private static class Builder implements DiameterHeader.Builder {

        private final WritableBuffer buffer;

        private Builder(final WritableBuffer buffer) {
            this.buffer = buffer;
            buffer.fastForwardWriterIndex();
        }

        @Override
        public DiameterHeader.Builder isRequest() {
            buffer.setBit7(4, true);
            return this;
        }

        @Override
        public DiameterHeader.Builder isAnswer() {
            buffer.setBit7(4, false);
            return this;
        }

        @Override
        public DiameterHeader.Builder withApplicationId(final long applicationId) {
            buffer.setUnsignedInt(8, applicationId);
            return this;
        }

        @Override
        public DiameterHeader.Builder withHopToHopId(final long id) {
            buffer.setUnsignedInt(12, id);
            return this;
        }

        @Override
        public DiameterHeader.Builder withEndToEndId(final long id) {
            buffer.setUnsignedInt(16, id);
            return this;
        }

        @Override
        public DiameterHeader.Builder withCommandCode(final int code) {
            buffer.setThreeOctetInt(5, code);
            return this;
        }

        @Override
        public DiameterHeader.Builder withLength(final int length) {
            buffer.setThreeOctetInt(1, length);
            return this;
        }

        @Override
        public DiameterHeader build() {
            buffer.setBit0(0, true); // set version to 1 - the only allowed version at this time.
            return new ImmutableDiameterHeader(buffer.build());
        }
    }

}
