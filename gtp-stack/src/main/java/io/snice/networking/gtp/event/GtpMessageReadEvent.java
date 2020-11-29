package io.snice.networking.gtp.event;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.common.Connection;
import io.snice.networking.gtp.Transaction;
import io.snice.networking.gtp.event.impl.DefaultGtpMessageEvent;

public interface GtpMessageReadEvent extends GtpMessageEvent {

    static GtpMessageReadEvent of(final GtpMessage msg, final Connection<GtpEvent> connection) {
        return DefaultGtpMessageEvent.newReadEvent(msg, connection);
    }

    static GtpMessageReadEvent of(final GtpMessage msg, final Transaction transaction) {
        return DefaultGtpMessageEvent.newReadEvent(msg, transaction);
    }


    @Override
    default boolean isMessageReadEvent() {
        return false;
    }

    @Override
    default GtpMessageReadEvent toMessageReadEvent() {
        return this;
    }


}
