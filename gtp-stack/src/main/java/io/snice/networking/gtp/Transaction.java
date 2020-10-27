package io.snice.networking.gtp;

import io.snice.codecs.codec.gtp.GtpRequest;
import io.snice.codecs.codec.gtp.GtpResponse;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Response;
import io.snice.networking.common.ConnectionId;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Transaction {

    TransactionIdentifier getId();

    /**
     * The original {@link GtpRequest} that started this transaction.
     */
    Gtp2Request getRequest();

    /**
     * The {@link ConnectionId} representing which underlying {@link GtpTunnel}
     * this {@link Transaction} is sent/received over (and ultimately, which connection
     * the tunnel is actually using)
     *
     * @return
     */
    ConnectionId getConnectionId();

    /**
     * <p>
     * Any data that the application has associated with this transaction.
     * <p>
     * Note that this data is completely transparent to the GTP stack.
     */
    Optional<Object> getApplicationData();

    BiConsumer<Transaction, Gtp2Response> getOnResponse();

    interface Builder {

        /**
         * Associate some arbitrary application data with this {@link Transaction}. This data is
         * completely transparent to the {@link Transaction} and the underlying GTP stack and
         * is not used in other way than acting as a place holder for this data so the application
         * can easily retrieve this data along with the transaction.
         * <p>
         * Note: you can only have one data object per transaction so if the application has to store
         * many things, wrap it in a list/map/object or whatever.
         */
        Builder withApplicationData(Object data);

        /**
         * Called when the {@link Transaction} is terminated and purged from memory, at which point any
         * re-transmissions will be considered a new transaction.
         * <p>
         * This callback will ALWAYS be the very last callback for this {@link Transaction} to occur.
         * No other callbacks will ever happen so it is safe for the application to also purge the reference
         * to this {@link Transaction} from memory.
         * <p>
         * Note that a {@link Transaction} will always at some point terminate, irrespective if the
         * transaction was successful or not.
         */
        Builder onTransactionTerminated(Consumer<Transaction> f);

        /**
         * Called once the {@link Transaction} receives any answer and as such, the transaction completes.
         * <p>
         * Note that any {@link GtpResponse} received for this transaction is considered to complete
         * the transaction successfully. No interpretation to whether the response indicates an error
         * or not is considered. It merely means that we did receive a response
         * to the request in a timely fashion.
         *
         * @param f the callback method that will be called upon successful completion
         *          of the {@link Transaction}. The function will take in the {@link Transaction}, which is
         *          this transaction (it's just really for easy access) and the {@link GtpResponse} that
         *          completed the transaction.
         * @return
         */
        Builder onAnswer(BiConsumer<Transaction, Gtp2Response> f);

        /**
         * Register a function to be called whenever a re-transmission occurs. If no function
         * has been registered, the GTP stack assumes the application simply isn't interested
         * and as such, any re-transmissions will silently be handled by the underlying stack.
         * <p>
         *
         * @param f
         * @return
         */
        Builder onRetransmission(Consumer<Transaction> f);

        /**
         * If the {@link Transaction} times out, this callback will be called.
         */
        Builder onTransactionTimeout(Consumer<Transaction> f);

        /**
         * Build and start the actual {@link Transaction}.
         *
         * @return
         */
        Transaction start();
    }

}
