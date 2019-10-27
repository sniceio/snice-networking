package io.snice.networking.diameter.avp.impl;

import io.snice.networking.diameter.avp.Avp;
import io.snice.networking.diameter.avp.FramedAvp;
import io.snice.networking.diameter.avp.type.Enumerated;

public class DiameterEnumeratedAvp<T extends Enum<T>> extends ImmutableAvp<Enumerated<T>> {

    @Override
    public boolean isEnumerated() {
        return true;
    }

    @Override
    public Avp<Enumerated<T>> toEnumerated() {
        return this;
    }

    public DiameterEnumeratedAvp(final FramedAvp raw, final Enumerated<T> e) {
        super(raw, e);
    }
}