package io.snice.networking.gtp.event;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.gtp.Transaction;
import io.snice.networking.gtp.event.impl.DefaultGtpMessageEvent;

public interface GtpMessageWriteEvent extends GtpMessageEvent {

    static GtpMessageWriteEvent of(final GtpMessage msg, final ConnectionId connectionId) {
        return DefaultGtpMessageEvent.newWriteEvent(msg, connectionId);
    }

    static GtpMessageWriteEvent of(final GtpMessage msg, final Transaction transaction) {
        return DefaultGtpMessageEvent.newWriteEvent(msg, transaction);
    }

    @Override
    ConnectionId getConnectionId();

    @Override
    default boolean isMessageWriteEvent() {
        return true;
    }

    @Override
    default GtpMessageWriteEvent toMessageWriteEvent() {
        return this;
    }

}
