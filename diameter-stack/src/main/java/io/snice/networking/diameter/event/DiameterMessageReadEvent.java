package io.snice.networking.diameter.event;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.diameter.event.impl.DefaultDiameterMessageEvent;
import io.snice.networking.diameter.tx.Transaction;

public interface DiameterMessageReadEvent extends DiameterMessageEvent {

    static DiameterMessageReadEvent of(final DiameterMessage msg) {
        return DefaultDiameterMessageEvent.newReadEvent(msg);
    }

    static DiameterMessageReadEvent of(final Transaction transaction) {
        return DefaultDiameterMessageEvent.newReadEvent(transaction);
    }

    static DiameterMessageReadEvent of(final DiameterMessage msg, final Transaction transaction) {
        return DefaultDiameterMessageEvent.newReadEvent(msg, transaction);
    }


    @Override
    default boolean isMessageReadEvent() {
        return true;
    }

    @Override
    default DiameterMessageReadEvent toMessageReadEvent() {
        return this;
    }
}
