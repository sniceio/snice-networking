package io.snice.networking.diameter.event;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.diameter.event.impl.DefaultDiameterMessageEvent;
import io.snice.networking.diameter.tx.Transaction;

public interface DiameterMessageWriteEvent extends DiameterMessageEvent {

    static DiameterMessageWriteEvent of(final DiameterMessage msg) {
        return DefaultDiameterMessageEvent.newWriteEvent(msg);
    }

    static DiameterMessageWriteEvent of(final Transaction transaction) {
        return DefaultDiameterMessageEvent.newWriteEvent(transaction);
    }

    @Override
    default boolean isMessageWriteEvent() {
        return true;
    }

    @Override
    default DiameterMessageWriteEvent toMessageWriteEvent() {
        return this;
    }
}
