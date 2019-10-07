package io.snice.networking.app;

import io.snice.buffer.Buffer;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

import java.util.function.Predicate;

public interface Bootstrap<T extends NetworkAppConfig> {

    T getConfiguration();

    /**
     * Every new incoming connection will be evaluated and configured for, if accepted,
     * future data across that {@link Connection}.
     *
     * Based on the condition, you can create different decision trees on what to do
     * with the incoming connection, such as drop it, drop and send some data back to
     * the remote end etc etc.
     *
     * @param condition
     * @return
     */
    ConnectionContext.Builder<Connection, Buffer, Buffer> onConnection(Predicate<ConnectionId> condition);

}
