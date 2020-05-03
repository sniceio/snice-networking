package io.snice.codecs.codec.diameter.avp.impl;

import io.snice.codecs.codec.diameter.avp.FramedAvp;
import io.snice.codecs.codec.diameter.avp.type.OctetString;

public class DiameterOctetStringAvp extends ImmutableAvp<OctetString> {

    public DiameterOctetStringAvp(final FramedAvp raw) {
        super(raw, OctetString.parse(raw.getData()));
    }

    public DiameterOctetStringAvp(final FramedAvp raw, final boolean isEncodedAsTBCD) {
        super(raw, OctetString.parse(raw.getData(), isEncodedAsTBCD));
    }
}
