package io.snice.networking.common.event;

import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.event.impl.IOEventImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ConnectionClosedIOEvent<T> extends ConnectionIOEvent<T> {

    @Override
    default boolean isConnectionClosedIOEvent() {
        return true;
    }

    @Override
    default ConnectionClosedIOEvent<T> toConnectionClosedIOEvent() {
        return this;
    }

    static <T> ConnectionClosedIOEvent<T> create(final ChannelContext<T> ctx, final long arrivalTime) {
        return new ConnectionClosedIOEventImpl(ctx, arrivalTime);
    }

    class ConnectionClosedIOEventImpl<T> extends IOEventImpl<T> implements ConnectionClosedIOEvent<T> {
        private ConnectionClosedIOEventImpl(final ChannelContext<T> ctx, final long arrivalTime) {
            super(ctx, arrivalTime);
        }
    }
}
