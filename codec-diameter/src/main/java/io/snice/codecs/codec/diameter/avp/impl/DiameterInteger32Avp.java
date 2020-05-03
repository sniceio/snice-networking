package io.snice.codecs.codec.diameter.avp.impl;

import io.snice.codecs.codec.diameter.avp.FramedAvp;
import io.snice.codecs.codec.diameter.avp.type.Integer32;

public class DiameterInteger32Avp extends ImmutableAvp<Integer32> {

    public DiameterInteger32Avp(final FramedAvp raw) {
        super(raw, Integer32.parse(raw.getData()));
    }
}
