package io.snice.networking.codec.diameter.avp.impl;

import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.type.Grouped;

public class DiameterGroupedAvp extends ImmutableAvp<Grouped> {

    public DiameterGroupedAvp(final FramedAvp raw) {
        super(raw, Grouped.parse(raw));
    }

    public DiameterGroupedAvp(final FramedAvp raw, final Grouped value) {
        super(raw, value);
    }
}
