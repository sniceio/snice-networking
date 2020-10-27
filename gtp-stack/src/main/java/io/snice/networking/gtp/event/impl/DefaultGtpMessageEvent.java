package io.snice.networking.gtp.event.impl;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.common.Connection;
import io.snice.networking.gtp.event.GtpMessageEvent;
import io.snice.networking.gtp.event.GtpMessageReadEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultGtpMessageEvent implements GtpMessageEvent {

    private final GtpMessage msg;
    private final Connection<GtpMessage> connection;

    public static GtpMessageReadEvent newReadEvent(final GtpMessage msg, final Connection<GtpMessage> connection) {
        assertNotNull(msg, "The gtp message cannot be null");
        assertNotNull(connection, "The connection cannot be null");
        return new MessageReadEvent(msg, connection);
    }

    public static GtpMessageWriteEvent newWriteEvent(final GtpMessage msg, final Connection<GtpMessage> connection) {
        assertNotNull(msg, "The gtp message cannot be null");
        assertNotNull(connection, "The connection cannot be null");
        return new MessageWriteEvent(msg, connection);
    }

    private DefaultGtpMessageEvent(final GtpMessage msg, final Connection<GtpMessage> connection) {
        this.msg = msg;
        this.connection = connection;
    }

    @Override
    public Connection<GtpMessage> getConnection() {
        return connection;
    }

    public GtpMessage getMessage() {
        return msg;
    }

    private static class MessageReadEvent extends DefaultGtpMessageEvent implements GtpMessageReadEvent {

        private MessageReadEvent(final GtpMessage msg, final Connection<GtpMessage> connection) {
            super(msg, connection);
        }

        @Override
        public boolean isMessageReadEvent() {
            return true;
        }
    }

    private static class MessageWriteEvent extends DefaultGtpMessageEvent implements GtpMessageWriteEvent {

        private MessageWriteEvent(final GtpMessage msg, final Connection<GtpMessage> connection) {
            super(msg, connection);
        }

        @Override
        public boolean isMessageWriteEvent() {
            return true;
        }
    }
}
