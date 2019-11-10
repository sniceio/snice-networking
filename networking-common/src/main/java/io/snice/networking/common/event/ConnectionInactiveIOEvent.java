package io.snice.networking.common.event;

import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.event.impl.IOEventImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ConnectionInactiveIOEvent<T> extends ConnectionIOEvent<T> {

    @Override
    default boolean isConnectionInactiveIOEvent() {
        return true;
    }

    static <T> ConnectionInactiveIOEvent<T> create(final ChannelContext<T> ctx, final long arrivalTime) {
        return new ConnectionInactiveIOEventImpl(ctx, arrivalTime);
    }

    class ConnectionInactiveIOEventImpl<T> extends IOEventImpl<T> implements ConnectionInactiveIOEvent<T> {
        private ConnectionInactiveIOEventImpl(final ChannelContext<T> ctx, final long arrivalTime) {
            super(ctx, arrivalTime);
        }
    }

}
