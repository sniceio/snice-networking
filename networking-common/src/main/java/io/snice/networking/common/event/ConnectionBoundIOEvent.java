package io.snice.networking.common.event;


import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.event.impl.IOEventImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ConnectionBoundIOEvent<T> extends ConnectionIOEvent<T> {
    @Override
    default boolean isConnectionBoundIOEvent() {
        return true;
    }

    static <T> ConnectionBoundIOEvent create(final ChannelContext<T> ctx, final long arrivalTime) {
        return new ConnectionBoundIOEventImpl(ctx, arrivalTime);
    }

    class ConnectionBoundIOEventImpl<T> extends IOEventImpl<T> implements ConnectionBoundIOEvent<T> {
        private ConnectionBoundIOEventImpl(final ChannelContext<T> ctx, final long arrivalTime) {
            super( ctx, arrivalTime);
        }
    }

}
