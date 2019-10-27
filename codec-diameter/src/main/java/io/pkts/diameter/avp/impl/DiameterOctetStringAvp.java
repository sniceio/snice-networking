package io.pkts.diameter.avp.impl;

import io.pkts.diameter.avp.FramedAvp;
import io.pkts.diameter.avp.type.OctetString;

public class DiameterOctetStringAvp extends ImmutableAvp<OctetString> {

    public DiameterOctetStringAvp(final FramedAvp raw) {
        super(raw, OctetString.parse(raw.getData()));
    }
}
