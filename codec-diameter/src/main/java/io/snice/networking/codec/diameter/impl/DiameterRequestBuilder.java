package io.snice.networking.codec.diameter.impl;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.diameter.DiameterHeader;
import io.snice.networking.codec.diameter.DiameterRequest;
import io.snice.networking.codec.diameter.avp.FramedAvp;

import java.util.List;

public class DiameterRequestBuilder extends DiameterMessageBuilder<DiameterRequest> {

    @Override
    public DiameterRequest build() {
        return null;
    }

    @Override
    protected DiameterRequest internalBuild(final Buffer message, final DiameterHeader header, final List<FramedAvp> avps, final short indexOfOriginHost, final short indexOfOriginRealm, final short indexOfDestinationHos, final short indexOfDestinationRealm) {
        return null;
    }
}
