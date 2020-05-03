package io.snice.codecs.codec.diameter.avp.impl;

import io.snice.codecs.codec.diameter.avp.FramedAvp;
import io.snice.codecs.codec.diameter.avp.type.UTF8String;

public class DiameterUtf8StringAvp extends ImmutableAvp<UTF8String> {

    public DiameterUtf8StringAvp(final FramedAvp raw) {
        super(raw, UTF8String.parse(raw.getData()));
    }
}
