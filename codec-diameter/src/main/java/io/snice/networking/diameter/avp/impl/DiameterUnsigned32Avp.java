package io.snice.networking.diameter.avp.impl;

import io.snice.networking.diameter.avp.FramedAvp;
import io.snice.networking.diameter.avp.type.Unsigned32;

public class DiameterUnsigned32Avp extends ImmutableAvp<Unsigned32> {

    public DiameterUnsigned32Avp(final FramedAvp raw) {
        super(raw, Unsigned32.parse(raw.getData()));
    }
}
