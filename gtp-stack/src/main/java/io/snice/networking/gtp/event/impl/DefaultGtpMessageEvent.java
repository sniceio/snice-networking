package io.snice.networking.gtp.event.impl;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.gtp.Transaction;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageEvent;
import io.snice.networking.gtp.event.GtpMessageReadEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultGtpMessageEvent implements GtpMessageEvent {

    private final GtpMessage msg;
    private final ConnectionId connectionId;
    private final Optional<Transaction> transaction;

    public static GtpMessageReadEvent newReadEvent(final GtpMessage msg, final Connection<GtpEvent> connection) {
        assertNotNull(msg, "The GTP message cannot be null");
        assertNotNull(connection, "The connection cannot be null");
        return new MessageReadEvent(msg, connection);
    }

    public static GtpMessageReadEvent newReadEvent(final GtpMessage msg, final Transaction transaction) {
        assertNotNull(transaction, "The GTP transaction cannot be null");
        return new MessageReadEvent(msg, transaction);
    }

    public static GtpMessageWriteEvent newWriteEvent(final GtpMessage msg, final ConnectionId connectionId) {
        assertNotNull(msg, "The GTP message cannot be null");
        assertNotNull(connectionId, "The connection cannot be null");
        return new MessageWriteEvent(msg, connectionId);
    }

    public static GtpMessageWriteEvent newWriteEvent(final GtpMessage msg, final Transaction transaction) {
        assertNotNull(transaction, "The GTP transaction cannot be null");
        assertNotNull(msg, "The GTP message cannot be null");
        return new MessageWriteEvent(msg, transaction);
    }

    private DefaultGtpMessageEvent(final GtpMessage msg, final ConnectionId connectionId) {
        this.msg = msg;
        this.connectionId = connectionId;
        this.transaction = Optional.empty();
    }

    private DefaultGtpMessageEvent(final GtpMessage msg, final Transaction transaction) {
        this.msg = msg;
        this.connectionId = transaction.getConnectionId();
        this.transaction = Optional.of(transaction);
    }

    @Override
    public Optional<Transaction> getTransaction() {
        return transaction;
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

        private MessageReadEvent(final GtpMessage msg, final Transaction transaction) {
            super(msg, transaction);
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

        private MessageWriteEvent(final GtpMessage msg, final Transaction transaction) {
            super(msg, transaction);
        }

        @Override
        public boolean isMessageWriteEvent() {
            return true;
        }
    }
}
