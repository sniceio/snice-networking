package io.snice.networking.gtp.event;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

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

    ConnectionId getConnectionId();

    @Override
    GtpMessage getMessage();
}
