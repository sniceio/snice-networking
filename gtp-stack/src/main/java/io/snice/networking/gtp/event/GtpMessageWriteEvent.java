package io.snice.networking.gtp.event;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.common.Connection;
import io.snice.networking.gtp.event.impl.DefaultGtpMessageEvent;

public interface GtpMessageWriteEvent extends GtpMessageEvent {

    static GtpMessageWriteEvent of(final GtpMessage msg, final Connection<GtpEvent> connection) {
        return DefaultGtpMessageEvent.newWriteEvent(msg, connection);
    }

    @Override
    default boolean isMessageWriteEvent() {
        return true;
    }

    @Override
    default GtpMessageWriteEvent toMessageWriteEvent() {
        return this;
    }

}
