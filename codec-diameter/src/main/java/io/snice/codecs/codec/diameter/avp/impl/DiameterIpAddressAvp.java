package io.snice.codecs.codec.diameter.avp.impl;

import io.snice.codecs.codec.diameter.avp.FramedAvp;
import io.snice.codecs.codec.diameter.avp.type.IpAddress;

public class DiameterIpAddressAvp extends ImmutableAvp<IpAddress> {

    public DiameterIpAddressAvp(final FramedAvp raw) {
        super(raw, IpAddress.parse(raw.getData()));
    }
}
