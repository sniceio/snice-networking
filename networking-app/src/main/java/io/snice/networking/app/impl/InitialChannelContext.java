package io.snice.networking.app.impl;

import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.ConnectionEndpointId;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.event.IOEvent;

/**
 * While a connection is being established we do not yet have a full contextd since
 * we may actually fail to establish the connection.
 *
 */
public class InitialChannelContext<T> implements ChannelContext<T> {

    public InitialChannelContext(final ConnectionEndpointId remote) {
    }

    @Override
    public ConnectionId getConnectionId() {
        return null;
    }

    @Override
    public void sendDownstream(final T msg) {
        throw new IllegalStateException("This ChannelContext has not been fully established yet");
    }

    @Override
    public void sendUpstream(final T msg) {
        throw new IllegalStateException("This ChannelContext has not been fully established yet");
    }

    @Override
    public void fireUserEvent(final IOEvent<T> evt) {
        throw new IllegalStateException("This ChannelContext has not been fully established yet");
    }

    @Override
    public void fireApplicationEvent(final Object evt) {
        throw new IllegalStateException("This ChannelContext has not been fully established yet");
    }
}
