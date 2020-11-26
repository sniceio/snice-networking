package io.snice.networking.gtp.event.impl;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageEvent;
import io.snice.networking.gtp.event.GtpMessageReadEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultGtpMessageEvent implements GtpMessageEvent {

    private final GtpMessage msg;
    private final ConnectionId connectionId;

    public static GtpMessageReadEvent newReadEvent(final GtpMessage msg, final Connection<GtpEvent> connection) {
        assertNotNull(msg, "The gtp message cannot be null");
        assertNotNull(connection, "The connection cannot be null");
        return new MessageReadEvent(msg, connection);
    }

    public static GtpMessageWriteEvent newWriteEvent(final GtpMessage msg, final ConnectionId connectionId) {
        assertNotNull(msg, "The gtp message cannot be null");
        assertNotNull(connectionId, "The connection cannot be null");
        return new MessageWriteEvent(msg, connectionId);
    }

    private DefaultGtpMessageEvent(final GtpMessage msg, final ConnectionId connectionId) {
        this.msg = msg;
        this.connectionId = connectionId;
    }

    @Deprecated
    @Override
    public Connection<GtpEvent> getConnection() {
        throw new IllegalArgumentException("Deprecated");
    }

    @Override
    public ConnectionId getConnectionId() {
        return connectionId;
    }

    @Override
    public GtpMessage getMessage() {
        return msg;
    }

    private static class MessageReadEvent extends DefaultGtpMessageEvent implements GtpMessageReadEvent {

        private MessageReadEvent(final GtpMessage msg, final Connection<GtpEvent> connection) {
            super(msg, connection.id());
        }

        @Override
        public boolean isMessageReadEvent() {
            return true;
        }
    }

    private static class MessageWriteEvent extends DefaultGtpMessageEvent implements GtpMessageWriteEvent {

        private MessageWriteEvent(final GtpMessage msg, final ConnectionId connectionId) {
            super(msg, connectionId);
        }

        @Override
        public boolean isMessageWriteEvent() {
            return true;
        }
    }
}
