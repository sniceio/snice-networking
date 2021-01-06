package io.snice.networking.app.impl;

import io.snice.networking.app.ConnectionContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.event.IOEvent;

public class DefaultChannelContext<T> implements InternalChannelContext<T> {

    private final Connection<T> connection;
    private final ConnectionContext<Connection<T>, T> ctx;

    public DefaultChannelContext(final Connection<T> connection, final ConnectionContext<Connection<T>, T> ctx) {
        this.connection = connection;
        this.ctx = ctx;
    }

    @Override
    public ConnectionId getConnectionId() {
        return connection.id();
    }

    @Override
    public ConnectionContext<Connection<T>, T> getConnectionContext() {
        return ctx;
    }

    @Override
    public void sendDownstream(final T msg) {
        connection.send(msg);
    }

    @Override
    public void sendUpstream(final T msg) {
        throw new RuntimeException("Sorry, not implemented yet but I really wanted to send this usptream " + msg);
    }

    @Override
    public void fireUserEvent(final IOEvent<T> evt) {
        throw new RuntimeException("Sorry, not implemented yet but I really wanted to send fire this event" + evt);
    }

    @Override
    public void fireApplicationEvent(final Object evt) {
        throw new RuntimeException("Sorry, not implemented yet but I really wanted to send fire this event" + evt);
    }
}
