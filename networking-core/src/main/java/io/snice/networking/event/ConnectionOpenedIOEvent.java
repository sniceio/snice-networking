package io.snice.networking.event;


import io.snice.networking.common.Connection;
import io.snice.networking.event.impl.IOEventImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ConnectionOpenedIOEvent extends ConnectionIOEvent {

    @Override
    default boolean isConnectionOpenedIOEvent() {
        return true;
    }

    static ConnectionOpenedIOEvent create(final Connection connection, final long arrivalTime) {
        return new ConnectionOpenedIOEventImpl(connection, arrivalTime);
    }

    class ConnectionOpenedIOEventImpl extends IOEventImpl implements ConnectionOpenedIOEvent {
        private ConnectionOpenedIOEventImpl(final Connection connection, final long arrivalTime) {
            super(connection, arrivalTime);
        }
    }
}
