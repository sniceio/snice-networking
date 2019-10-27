package io.snice.networking.diameter.avp.impl;

import io.snice.networking.diameter.avp.FramedAvp;
import io.snice.networking.diameter.avp.type.UTF8String;

public class DiameterUtf8StringAvp extends ImmutableAvp<UTF8String> {

    public DiameterUtf8StringAvp(final FramedAvp raw) {
        super(raw, UTF8String.parse(raw.getData()));
    }
}
