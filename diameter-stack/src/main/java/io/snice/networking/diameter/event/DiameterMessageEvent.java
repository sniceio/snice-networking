package io.snice.networking.diameter.event;

import io.snice.codecs.codec.diameter.DiameterAnswer;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.networking.diameter.event.impl.DefaultDiameterMessageEvent;

public interface DiameterMessageEvent extends DiameterEvent {

    static DiameterMessageEvent of(final DiameterMessage msg) {
        return DefaultDiameterMessageEvent.of(msg);
    }

    @Override
    default boolean isMessageEvent() {
        return true;
    }

    @Override
    default DiameterMessageEvent toMessageEvent() {
        return this;
    }

}
