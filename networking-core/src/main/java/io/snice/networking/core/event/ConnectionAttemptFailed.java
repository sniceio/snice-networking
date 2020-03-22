package io.snice.networking.core.event;

import java.util.Optional;

/**
 * Represents an attempt made by the user to connect to a remote host that
 * ultimately failed.
 */
public interface ConnectionAttemptFailed<T> extends ConnectionAttempt<T> {

    @Override
    default boolean isConnectionAttemptEventFailed() {
        return true;
    }

    @Override
    default ConnectionAttemptFailed<T> toConnectionAttemptFailed() {
        return this;
    }

    /**
     * The underlying cause, if present, of why it failed.
     */
    Optional<Throwable> getCause();

}
