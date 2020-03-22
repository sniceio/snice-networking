package io.snice.networking.common.event;


import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.event.impl.IOEventImpl;

/**
 * We are attempting to connect to a remote location, which may or may not succeed.
 *
 * @author jonas@jonasborjesson.com
 */
public interface ConnectionConnectAttemptIOEvent<T> extends ConnectionIOEvent<T> {

    default ConnectionConnectAttemptIOEvent toConnectionConnectAttemptIOEvent() {
        return this;
    }

    default boolean isConnectionConnectAttemptIOEvent() {
        return true;
    }

    static <T> ConnectionConnectAttemptIOEvent create(final ChannelContext<T> ctx, final long arrivalTime) {
        return new ConnectionConnectAttemptIOEventImpl(ctx, arrivalTime);
    }

    class ConnectionConnectAttemptIOEventImpl<T> extends IOEventImpl<T> implements ConnectionConnectAttemptIOEvent<T> {
        private ConnectionConnectAttemptIOEventImpl(final ChannelContext<T> ctx, final long arrivalTime) {
            super( ctx, arrivalTime);
        }
    }

}
