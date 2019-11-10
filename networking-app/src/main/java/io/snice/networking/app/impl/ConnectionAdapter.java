package io.snice.networking.app.impl;

import io.snice.networking.app.ConnectionContext;
import io.snice.networking.common.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionAdapter<C extends Connection<T>, T> {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionAdapter.class);

    private final C connection;
    private final ConnectionContext<C, T> ctx;

    public ConnectionAdapter(final C connection, final ConnectionContext<C, T> ctx) {
        this.connection = connection;
        this.ctx = ctx;
    }

    public void process(final T data) {
        ctx.match(connection, data).apply(connection, data);
    }
}
