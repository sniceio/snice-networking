package io.snice.networking.core.event;

import io.snice.networking.common.event.IOEvent;

/**
 * The family of {@link NetworkEvent}s are low-level events that are emitted
 * by the "raw" network layer. These events will typically be transformed to {@link IOEvent}s
 * as they traverse the network stack and eventually arrive at the user application. No
 * {@link NetworkEvent}s should make it to the user application. They must be translated
 * before they are handed off.
 */
public interface NetworkEvent<T> {

    default boolean isConnectionAttemptEvent() {
        return false;
    }

    default boolean isConnectionAttemptEventFailed() {
        return false;
    }

    default boolean isConnectionAttemptEventSuccess() {
        return false;
    }

    default ConnectionAttempt<T> toConnectionAttempt() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + ConnectionAttempt.class.getName());
    }

    default ConnectionAttemptFailed<T> toConnectionAttemptFailed() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + ConnectionAttemptFailed.class.getName());
    }

    default ConnectionAttemptSuccess<T> toConnectionAttemptSuccess() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + ConnectionAttemptSuccess.class.getName());
    }
}
