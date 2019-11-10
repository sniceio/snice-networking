package io.snice.networking.common.event;

import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.event.impl.IOEventImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ConnectionOpenedIOEvent<T> extends ConnectionIOEvent<T> {

    @Override
    default boolean isConnectionOpenedIOEvent() {
        return true;
    }

    static <T> ConnectionOpenedIOEvent create(final ChannelContext<T> connection, final long arrivalTime) {
        return new ConnectionOpenedIOEventImpl(connection, arrivalTime);
    }

    class ConnectionOpenedIOEventImpl<T> extends IOEventImpl<T> implements ConnectionOpenedIOEvent<T> {
        private ConnectionOpenedIOEventImpl(final ChannelContext<T> connection, final long arrivalTime) {
            super(connection, arrivalTime);
        }
    }
}
