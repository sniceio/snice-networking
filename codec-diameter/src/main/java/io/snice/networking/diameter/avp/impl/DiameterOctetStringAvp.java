package io.snice.networking.diameter.avp.impl;

import io.snice.networking.diameter.avp.FramedAvp;
import io.snice.networking.diameter.avp.type.OctetString;

public class DiameterOctetStringAvp extends ImmutableAvp<OctetString> {

    public DiameterOctetStringAvp(final FramedAvp raw) {
        super(raw, OctetString.parse(raw.getData()));
    }
}
