package io.snice.codecs.codec.diameter.avp.impl;

import io.snice.codecs.codec.diameter.avp.FramedAvp;
import io.snice.codecs.codec.diameter.avp.type.Unsigned32;

public class DiameterUnsigned32Avp extends ImmutableAvp<Unsigned32> {

    public DiameterUnsigned32Avp(final FramedAvp raw) {
        super(raw, Unsigned32.parse(raw.getData()));
    }
}
