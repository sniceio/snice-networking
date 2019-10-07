package io.snice.networking.event;


import io.snice.networking.common.Connection;
import io.snice.networking.event.impl.IOEventImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ConnectionInactiveIOEvent extends ConnectionIOEvent {

    @Override
    default boolean isConnectionInactiveIOEvent() {
        return true;
    }

    static ConnectionInactiveIOEvent create(final Connection connection, final long arrivalTime) {
        return new ConnectionInactiveIOEventImpl(connection, arrivalTime);
    }

    class ConnectionInactiveIOEventImpl extends IOEventImpl implements ConnectionInactiveIOEvent {
        private ConnectionInactiveIOEventImpl(final Connection connection, final long arrivalTime) {
            super(connection, arrivalTime);
        }
    }

}
