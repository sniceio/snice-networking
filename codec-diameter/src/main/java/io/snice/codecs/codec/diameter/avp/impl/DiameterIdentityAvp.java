package io.snice.codecs.codec.diameter.avp.impl;

import io.snice.codecs.codec.diameter.avp.FramedAvp;
import io.snice.codecs.codec.diameter.avp.type.DiameterIdentity;

public class DiameterIdentityAvp extends ImmutableAvp<DiameterIdentity> {

    public DiameterIdentityAvp(final FramedAvp raw) {
        super(raw, DiameterIdentity.parse(raw.getData()));
    }
}
