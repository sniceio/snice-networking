package io.snice.networking.event;


import io.snice.networking.common.Connection;
import io.snice.networking.event.impl.IOEventImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ConnectionClosedIOEvent extends ConnectionIOEvent {

    @Override
    default boolean isConnectionClosedIOEvent() {
        return true;
    }

    static ConnectionClosedIOEvent create(final Connection connection, final long arrivalTime) {
        return new ConnectionClosedIOEventImpl(connection, arrivalTime);
    }

    class ConnectionClosedIOEventImpl extends IOEventImpl implements ConnectionClosedIOEvent {
        private ConnectionClosedIOEventImpl(final Connection connection, final long arrivalTime) {
            super(connection, arrivalTime);
        }
    }
}
