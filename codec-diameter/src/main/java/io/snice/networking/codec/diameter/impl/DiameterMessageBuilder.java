package io.snice.networking.codec.diameter.impl;

import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.api.DestinationHost;
import io.snice.networking.codec.diameter.avp.api.DestinationRealm;
import io.snice.networking.codec.diameter.avp.api.OriginHost;
import io.snice.networking.codec.diameter.avp.api.OriginRealm;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class DiameterMessageBuilder<T extends DiameterMessage> implements DiameterMessage.Builder<T> {
    @Override
    public DiameterMessage.Builder<T> onAvp(final Function<Avp, Avp> f) throws IllegalStateException {
        return this;
    }

    @Override
    public DiameterMessage.Builder<T> withOriginHost(final OriginHost originHost) {
        return this;
    }

    @Override
    public DiameterMessage.Builder<T> withOriginRealm(final OriginRealm originHost) {
        return this;
    }

    @Override
    public DiameterMessage.Builder<T> withDestinationHost(final DestinationHost destHost) {
        return this;
    }

    @Override
    public DiameterMessage.Builder<T> withDestinationRealm(final DestinationRealm destRealm) {
        return this;
    }

    @Override
    public DiameterMessage.Builder<T> withAvp(final Avp avp) {
        return this;
    }

    @Override
    public DiameterMessage.Builder<T> onCommit(final Consumer<DiameterMessage> f) {
        return this;
    }
}
