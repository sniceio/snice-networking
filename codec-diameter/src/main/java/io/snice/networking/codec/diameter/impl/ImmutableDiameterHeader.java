package io.snice.networking.codec.diameter.impl;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.diameter.DiameterHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public class ImmutableDiameterHeader implements DiameterHeader {
    private final Buffer buffer;

    protected ImmutableDiameterHeader(final Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int getLength() {
        return (buffer.getByte(1) & 0xff) << 16
                | (buffer.getByte(2) & 0xff) << 8 | (buffer.getByte(3) & 0xff) << 0;
    }

    @Override
    public boolean isRequest() {
        return false;
    }

    @Override
    public boolean isProxiable() {
        return false;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public boolean isPossiblyRetransmission() {
        return false;
    }

    @Override
    public long getApplicationId() {
        return 0;
    }

    @Override
    public long getHopByHopId() {
        return 0;
    }

    @Override
    public long getEndToEndId() {
        return 0;
    }

}
