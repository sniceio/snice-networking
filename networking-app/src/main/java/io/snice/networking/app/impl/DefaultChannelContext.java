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

    @Override
    public void sendDownstream(T msg) {
        connection.send(msg);
    }

    @Override
    public void sendUpstream(T msg) {
        System.err.println("Yay, sending the crap upstream");
    }
}
