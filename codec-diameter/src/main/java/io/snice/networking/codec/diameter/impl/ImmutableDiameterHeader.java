package io.snice.networking.codec.diameter.impl;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.diameter.DiameterHeader;

/**
 *
 * @author jonas@jonasborjesson.com
 */
public class ImmutableDiameterHeader implements DiameterHeader {
    private final Buffer buffer;

    protected ImmutableDiameterHeader(final Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int getLength() {
        return DiameterParser.getIntFromThreeOctets(getByte(1), getByte(2), getByte(3));
    }

    @Override
    public boolean isRequest() {
        // 5th byte is the command flags
        return (getByte(4) & 0b10000000) == 0b10000000;
    }

    @Override
    public boolean isProxiable() {
        return (getByte(4) & 0b01000000) == 0b01000000;
    }

    @Override
    public boolean isError() {
        return (getByte(4) & 0b00100000) == 0b00100000;
    }

    @Override
    public boolean isPossiblyRetransmission() {
        return (getByte(4) & 0b00010000) == 0b00010000;
    }

    @Override
    public int getCommandCode() {
        return DiameterParser.getIntFromThreeOctets(getByte(5), getByte(6), getByte(7));
    }

    @Override
    public long getApplicationId() {
        return getLong(8);
    }

    @Override
    public long getHopByHopId() {
        return getLong(12);
    }

    @Override
    public long getEndToEndId() {
        return getLong(16);
    }

    private final byte getByte(final int index) {
            return buffer.getByte(index);
    }

    @Override
    public boolean validate() {
        // the version must be 1 so if it isn't, bail out.
        if (!((getByte(0) & 0b00000001) == 0b00000001)) {
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

    private long getLong(final int i) {
        return (getByte(i) & 0xff) << 24 | (getByte(i + 1) & 0xff) << 16
                | (getByte(i + 2) & 0xff) << 8 | (getByte(i + 3) & 0xff) << 0;
    }

}
