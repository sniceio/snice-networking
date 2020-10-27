package io.snice.networking.gtp.event;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.common.Connection;
import io.snice.networking.gtp.event.impl.DefaultGtpMessageEvent;

public interface GtpMessageReadEvent extends GtpMessageEvent {

    static GtpMessageReadEvent of(final GtpMessage msg, final Connection<GtpMessage> connection) {
        return DefaultGtpMessageEvent.newReadEvent(msg, connection);
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
