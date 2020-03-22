package io.snice.networking.common.event;

import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.Connection;

/**
 * All {@link IOEvent}s are "higher-level" events and is being created & managed by the
 * transport adapters. The lowest level of the stack will emit "raw" events
 *
 * @author jonas@jonasborjesson.com
 */
public interface IOEvent<T> {

    /**
     * The {@link Connection} over which this event took place.
     *
     * @return
     */
    Connection<T> connection();

    ChannelContext<T> channelContext();

    /**
     * The time at which this event took place. If the event came off of the network
     * then this is the time at which that event had been read off of the socket.
     *
     * @return
     */
    long arrivalTime();

    /**
     * Events concerning the state of a connection will be delivered via ConnectionIOEvents.
     * Check if this IOEvent is a connection event.
     *
     * @return
     */
    default boolean isConnectionIOEvent() {
        return false;
    }

    default boolean isConnectionConnectAttemptIOEvent() {
        return false;
    }

    default boolean isConnectionOpenedIOEvent() {
        return false;
    }

    default boolean isConnectionClosedIOEvent() {
        return false;
    }

    default boolean isConnectionCloseIOEvent() {
        return false;
    }

    default boolean isConnectionActiveIOEvent() {
        return false;
    }

    default boolean isConnectionInactiveIOEvent() {
        return false;
    }

    default boolean isConnectionBoundIOEvent() {
        return false;
    }

    default boolean isTimeoutEvent() {
        return false;
    }

    default boolean isMessageIOEvent() {
        return false;
    }

    default boolean isConnectionAttemptCompletedIOEvent() {
        return false;
    }

    default ConnectionIOEvent toConnectionIOEvent() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + ConnectionIOEvent.class.getName());
    }

    default ConnectionConnectAttemptIOEvent toConnectionConnectAttemptIOEvent() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + ConnectionConnectAttemptIOEvent.class.getName());
    }
    default MessageIOEvent<T> toMessageIOEvent() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + MessageIOEvent.class.getName());
    }

    default ConnectionClosedIOEvent<T> toConnectionClosedIOEvent() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + ConnectionClosedIOEvent.class.getName());
    }

    default ConnectionAttemptCompletedIOEvent<T> toConnectionAttemptCompletedIOEvent() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + ConnectionAttemptCompletedIOEvent.class.getName());
    }

}
