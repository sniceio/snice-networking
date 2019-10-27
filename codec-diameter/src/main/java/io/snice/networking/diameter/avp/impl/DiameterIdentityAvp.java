package io.snice.networking.diameter.avp.impl;

import io.snice.networking.diameter.avp.FramedAvp;
import io.snice.networking.diameter.avp.type.DiameterIdentity;

public class DiameterIdentityAvp extends ImmutableAvp<DiameterIdentity> {

    public DiameterIdentityAvp(final FramedAvp raw) {
        super(raw, DiameterIdentity.parse(raw.getData()));
    }
}
