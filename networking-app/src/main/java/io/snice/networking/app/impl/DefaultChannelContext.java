package io.snice.networking.app.impl;

import io.snice.networking.app.ConnectionContext;
import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

public class DefaultChannelContext<T> implements ChannelContext<T> {

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

    public ConnectionContext<Connection<T>, T> getConnectionContext() {
        return ctx;
    }

    @Override
    public void sendDownstream(final T msg) {
        connection.send(msg);
    }

    @Override
    public void sendUpstream(final T msg) {
        System.err.println("Yay, sending the crap upstream");
    }
}
