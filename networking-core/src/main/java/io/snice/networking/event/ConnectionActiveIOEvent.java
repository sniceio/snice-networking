package io.snice.networking.event;

import io.snice.networking.common.Connection;
import io.snice.networking.event.impl.IOEventImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ConnectionActiveIOEvent extends ConnectionIOEvent {
    @Override
    default boolean isConnectionActiveIOEvent() {
        return true;
    }

    static ConnectionActiveIOEvent create(final Connection connection, final long arrivalTime) {
        return new ConnectionActiveIOEventImpl(connection, arrivalTime);
    }

    class ConnectionActiveIOEventImpl extends IOEventImpl implements ConnectionActiveIOEvent {
        private ConnectionActiveIOEventImpl(final Connection connection, final long arrivalTime) {
            super(connection, arrivalTime);
        }
    }
}
