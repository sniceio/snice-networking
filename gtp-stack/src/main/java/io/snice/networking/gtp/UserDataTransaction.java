package io.snice.networking.gtp;

import io.snice.buffer.Buffer;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Allow a user to send their "own" data in a transaction when using {@link EpsBearer} to send that data.
 * <p>
 * The main semantic difference between this {@link UserDataTransaction} and the {@link Transaction} is that
 * while the {@link Transaction} is focusing on GTP transactions and as such, know how those "work" and also they
 * belong to to the GTP Stack, while the purpose of the {@link UserDataTransaction} is to allow a user to get
 * transaction help for their data. However, since the underlying GTP Stack do not know anything about the protocol
 * running across the user plane, the customer will have to specify those.
 */
public interface UserDataTransaction<T> {

    TransactionIdentifier getId();


    /**
     * Create a new {@link UserDataTransaction} for the given data and configure that transaction
     * through the {@link Builder} object, where you have to register factories for decoding and
     * calculating {@link TransactionIdentifier}s etc.
     *
     * @param data
     * @return
     */
    Builder<T> of(T data);

    /**
     * The {@link ConnectionId} representing which underlying {@link Connection}
     * this {@link UserDataTransaction} is sent/received over
     *
     * @return
     */
    ConnectionId getConnectionId();

    interface Builder<T> {

        /**
         * Unless the type is a {@link Buffer} you must supply a decoder to decode the raw buffer
         * into an instance of the given type.
         */
        Builder<T> withDecoder(Function<Buffer, T> decoder);

        /**
         * In order to be able to determine which
         *
         * @param decoder
         * @return
         */
        Builder<T> withTransactionDecoder(Function<T, TransactionIdentifier> decoder);

        /**
         * Called when the {@link UserDataTransaction} is terminated and purged from memory, at which point any
         * re-transmissions will be considered a new transaction and delivered as a "regular" PDU to the
         * GTP application.
         * <p>
         * This callback will ALWAYS be the very last callback for this {@link UserDataTransaction} to occur.
         * No other callbacks will ever happen so it is safe for the application to also purge the reference
         * to this {@link UserDataTransaction} from memory.
         * <p>
         * Note that a {@link UserDataTransaction} will always at some point terminate, irrespective if the
         * transaction was successful or not.
         */
        Builder onTransactionTerminated(Consumer<UserDataTransaction<T>> f);

        /**
         * Specify a transaction timeout and if the transaction do timeout,
         * the registered {@link #onTransactionTimeout(Consumer)} callback will be invoked.
         *
         * @param timeout
         * @return
         */
        Builder<T> withTransactionTimeout(Duration timeout);

        /**
         * If a transaction timeout has been specified, register a callback to be invoked
         * if the transaction do indeed timeout.
         */
        Builder<T> onTransactionTimeout(Consumer<UserDataTransaction<T>> f);

        /**
         * Whenever a response is detected it will be delivered to the application through
         * this registered function.
         */
        Builder onResponse(BiConsumer<UserDataTransaction, T> f);

        /**
         * Start this transaction, which will convert the given object (unless already done) to
         * a {@link Buffer} that will be sent across the SGi interface.
         *
         * @return
         */
        UserDataTransaction<T> start();
    }
}
