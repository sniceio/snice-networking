package io.snice.networking.codec.diameter.avp.impl;

import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.type.OctetString;

public class DiameterOctetStringAvp extends ImmutableAvp<OctetString> {

    public DiameterOctetStringAvp(final FramedAvp raw) {
        super(raw, OctetString.parse(raw.getData()));
    }
}
