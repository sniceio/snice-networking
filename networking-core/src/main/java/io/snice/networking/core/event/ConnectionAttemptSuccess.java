package io.snice.networking.core.event;

import io.snice.networking.common.Connection;

/**
 * Represents an attempt made by the user to connect to a remote host that
 * succeeded.
 */
public interface ConnectionAttemptSuccess<T> extends ConnectionAttempt<T> {

    @Override
    default boolean isConnectionAttemptEventSuccess() {
        return true;
    }

    @Override
    default ConnectionAttemptSuccess<T> toConnectionAttemptSuccess() {
        return this;
    }

    /**
     * The {@link Connection} that was established as a result of the connection attempt.
     *
     */
    Connection<T> getConnection();

}
