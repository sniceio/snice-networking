package io.snice.networking.diameter.avp.impl;

import io.snice.networking.diameter.avp.FramedAvp;
import io.snice.networking.diameter.avp.type.Grouped;

public class DiameterGroupedAvp extends ImmutableAvp<Grouped> {

    public DiameterGroupedAvp(final FramedAvp raw) {
        super(raw, Grouped.parse(raw));
    }
}
