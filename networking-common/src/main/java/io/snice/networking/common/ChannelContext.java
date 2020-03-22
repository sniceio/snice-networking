package io.snice.networking.common;

import io.snice.networking.common.event.IOEvent;

public interface ChannelContext<T> {

    ConnectionId getConnectionId();

    /**
     * Send the given message down the chain of handlers. Unless
     * dropped along the way, this will eventually write the message
     * to the underlying network.
     *
     * @param msg
     */
    void sendDownstream(T msg);

    /**
     * Forward the message up the chain of handlers. Unless
     * dropped along the way, this will eventually reach the
     * application.
     *
     * @param msg
     */
    void sendUpstream(T msg);

    /**
     * Fire a user event, which will propagate the event up the handler chain.
     */
    void fireUserEvent(IOEvent<T> evt);


}
