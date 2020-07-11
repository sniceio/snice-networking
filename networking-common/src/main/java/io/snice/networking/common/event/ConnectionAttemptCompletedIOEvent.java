package io.snice.networking.common.event;


import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.event.impl.IOEventImpl;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The result of an attempt to connect to a remote host.
 *
 * @author jonas@jonasborjesson.com
 */
public interface ConnectionAttemptCompletedIOEvent<T> extends ConnectionIOEvent<T> {

    @Override
    default boolean isConnectionAttemptCompletedIOEvent() {
        return true;
    }

    @Override
    default ConnectionAttemptCompletedIOEvent<T> toConnectionAttemptCompletedIOEvent() {
        return this;
    }

    /**
     * Whether or not the connection attempt was successful or not.
     */
    default boolean isSuccess() {
        return getConnection().isPresent();
    }

    /**
     * Depending on the implementing protocol, it may be that after e.g. a TCP connection
     * had successfully been established and the underlying Snice Newtworking layer produced
     * a successful {@link ConnectionAttemptCompletedIOEvent}, it could be that it should
     * fail because of other business logic.
     *
     * E.g., in Diameter, after you establish a "connection" the two sides exchange messages to
     * establish a common "ground". If this fails, then the "connection" (called Peer in Diameter)
     * should also fail and as such, the original success attempt will be turned into a failed on.
     *
     * @return a failed {@link ConnectionAttemptCompletedIOEvent}
     */
    ConnectionAttemptCompletedIOEvent<T> fail(Throwable cause);

    /**
     * If successful, the {@link Connection} that was established.
     */
    Optional<Connection<T>> getConnection();

    /**
     * The underlying cause to why we were unable to establish the connection.
     *
     */
    Optional<Throwable> getCause();

    CompletableFuture<Connection<T>> getUserFuture();


    static <T> ConnectionAttemptCompletedIOEvent<T> create(final ChannelContext<T> ctx, final CompletableFuture<Connection<T>> userFuture, final Connection<T> connection, final long arrivalTime) {
        return new ConnectionAttemptCompletedIOEventImpl<>(ctx, userFuture, connection, arrivalTime);
    }

    static <T> ConnectionAttemptCompletedIOEvent failure(final ChannelContext<T> ctx,final CompletableFuture<Connection<T>> userFuture, final InetSocketAddress remoteAddress, final Throwable cause, final long arrivalTime) {
        return new ConnectionAttemptCompletedIOEventImpl<>(ctx, userFuture, remoteAddress, cause, arrivalTime);
    }

    class ConnectionAttemptCompletedIOEventImpl<T> extends IOEventImpl<T> implements ConnectionAttemptCompletedIOEvent<T> {

        /**
         * This is the future that was given out to the user when they requested to establish the connection.
         */
        private final CompletableFuture<Connection<T>> userFuture;

        private final Optional<Connection<T>> connection;

        private final Optional<Throwable> cause;

        private ConnectionAttemptCompletedIOEventImpl(final ChannelContext<T> ctx, final CompletableFuture<Connection<T>> userFuture, final Connection<T> connection, final long arrivalTime) {
            super(ctx, arrivalTime);
            this.userFuture = userFuture;
            this.connection = Optional.of(connection);
            cause = Optional.empty();
        }

        private ConnectionAttemptCompletedIOEventImpl(final ChannelContext<T> ctx, final CompletableFuture<Connection<T>> userFuture, final InetSocketAddress remoteAddress, final Throwable cause, final long arrivalTime) {
            super(ctx, arrivalTime);
            this.userFuture = userFuture;
            connection = Optional.empty();
            this.cause = Optional.of(cause);
        }

        @Override
        public ConnectionAttemptCompletedIOEvent<T> fail(final Throwable cause) {
            return new ConnectionAttemptCompletedIOEventImpl<>(channelContext(), userFuture, null, cause, arrivalTime());
        }

        @Override
        public Optional<Connection<T>> getConnection() {
            return connection;
        }

        @Override
        public Optional<Throwable> getCause() {
            return cause;
        }

        @Override
        public CompletableFuture<Connection<T>> getUserFuture() {
            return userFuture;
        }
    }
}
