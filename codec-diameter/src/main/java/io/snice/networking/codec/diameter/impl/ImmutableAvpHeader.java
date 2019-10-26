package io.snice.networking.codec.diameter.impl;

import io.snice.networking.codec.diameter.AvpHeader;

import java.util.Optional;

/**
 * @author jonas@jonasborjesson.com
 */
public class ImmutableAvpHeader implements AvpHeader {

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public Optional<Long> getVendorId() {
        return null;
    }
}
