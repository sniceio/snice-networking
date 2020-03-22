package io.snice.networking.core.event;

import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionEndpointId;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * Represents an attempt made by the user to connect to a remote host.
 */
public interface ConnectionAttempt<T> extends NetworkEvent<T> {

    /**
     * The remote endpoint, to which the user is trying to connect to.
     */
    ConnectionEndpointId getRemoteEndpoint();

    /**
     * The future that the user is waiting to have completed, success of fail.
     * This is the future that is given out in {@link io.snice.networking.core.ListeningPoint#connect(InetSocketAddress)}
     * and will have to be completed by one of the network layers.
     *
     * Note: it must be completed on a thread that belongs to the application layer.
     */
    CompletableFuture<Connection<T>> getUserConnectionFuture();

    /**
     * The time this event "arrived" meaning when the connection attempt was made or, consequently,
     * concluded with a failure or success.
     * @return
     */
    long getArrivalTime();

    @Override
    default boolean isConnectionAttemptEvent() {
        return true;
    }

    @Override
    default ConnectionAttempt<T> toConnectionAttempt() {
        return this;
    }

    static <T> ConnectionAttemptSuccess<T> success(final CompletableFuture<Connection<T>> userFuture, final Connection<T> connection, final long arrivalTime) {
        assertNotNull(userFuture);
        assertNotNull(connection);
        assertArgument(arrivalTime > 0, "The arrival time must be greater than zero");
        return new ConnectionAttemptSuccessImpl<>(userFuture, connection, arrivalTime);
    }

    static <T> ConnectionAttemptFailed<T> failure(final CompletableFuture<Connection<T>> userFuture, final ConnectionEndpointId remoteId, final Throwable cause, final long arrivalTime) {
        assertNotNull(userFuture);
        assertNotNull(remoteId);
        assertArgument(arrivalTime > 0, "The arrival time must be greater than zero");
        return new ConnectionAttemptFailedImpl<>(userFuture, remoteId, cause, arrivalTime);
    }

    class ConnectionAttemptImpl<T> implements ConnectionAttempt<T> {

        private final CompletableFuture<Connection<T>> userFuture;
        private final ConnectionEndpointId remoteEndpoint;
        private final long arrivalTime;

        private ConnectionAttemptImpl(final CompletableFuture<Connection<T>> userFuture, final ConnectionEndpointId remoteEndpoint, final long arrivalTime) {
            this.userFuture = userFuture;
            this.remoteEndpoint = remoteEndpoint;
            this.arrivalTime = arrivalTime;
        }

        @Override
        public ConnectionEndpointId getRemoteEndpoint() {
            return remoteEndpoint;
        }

        @Override
        public CompletableFuture<Connection<T>> getUserConnectionFuture() {
            return userFuture;
        }

        @Override
        public long getArrivalTime() {
            return arrivalTime;
        }
    }

    class ConnectionAttemptFailedImpl<T> extends ConnectionAttemptImpl<T> implements ConnectionAttemptFailed<T> {

        private final Optional<Throwable> cause;

        private ConnectionAttemptFailedImpl(final CompletableFuture<Connection<T>> userFuture,
                                            final ConnectionEndpointId remoteEndpoint,
                                            final Throwable cause,
                                            final long arrivalTime) {
            super(userFuture, remoteEndpoint, arrivalTime);
            this.cause = Optional.ofNullable(cause);
        }

        @Override
        public Optional<Throwable> getCause() {
            return cause;
        }
    }

    class ConnectionAttemptSuccessImpl<T> extends ConnectionAttemptImpl<T> implements ConnectionAttemptSuccess<T> {

        private final Connection<T> connection;

        private ConnectionAttemptSuccessImpl(final CompletableFuture<Connection<T>> userFuture,
                                             final Connection<T> connection,
                                            final long arrivalTime) {
            super(userFuture, connection.id().getRemoteConnectionEndpointId(), arrivalTime);
            this.connection = connection;
        }

        @Override
        public Connection<T> getConnection() {
            return connection;
        }
    }

}
