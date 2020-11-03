package io.snice.networking.gtp.event;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.common.Connection;

public interface GtpMessageEvent extends GtpEvent {

    @Override
    default boolean isMessageEvent() {
        return true;
    }

    @Override
    default GtpMessageEvent toMessageEvent() {
        return this;
    }

    Connection<GtpEvent> getConnection();

    GtpMessage getMessage();
}
