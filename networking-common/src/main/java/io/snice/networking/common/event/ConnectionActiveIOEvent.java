package io.snice.networking.common.event;


import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.event.impl.IOEventImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ConnectionActiveIOEvent<T> extends ConnectionIOEvent<T> {
    @Override
    default boolean isConnectionActiveIOEvent() {
        return true;
    }

    static <T> ConnectionActiveIOEvent<T> create(final ChannelContext<T> ctx, final long arrivalTime) {
        return new ConnectionActiveIOEventImpl(ctx, arrivalTime);
    }

    class ConnectionActiveIOEventImpl<T> extends IOEventImpl<T> implements ConnectionActiveIOEvent<T> {
        private ConnectionActiveIOEventImpl(final ChannelContext<T> ctx, final long arrivalTime) {
            super(ctx, arrivalTime);
        }
    }
}
