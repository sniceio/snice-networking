package io.snice.networking.diameter.tx;

import io.snice.codecs.codec.diameter.DiameterAnswer;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.codecs.codec.diameter.TransactionIdentifier;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Every {@link DiameterMessage} will go within a {@link Transaction}.
 */
public interface Transaction {

    TransactionIdentifier getId();

    /**
     * The original {@link DiameterRequest} that started this transaction.
     */
    DiameterRequest getRequest();

    /**
     * <p>
     * Any data that the application has associated with this transaction.
     * <p>
     * Note that this data is completely transparent to the diameter stack.
     */
    Optional<Object> getApplicationData();

    BiConsumer<Transaction, DiameterAnswer> getOnAnswer();

    interface Builder {

        /**
         * Associate some arbitrary application data with this {@link Transaction}. This data is
         * completely transparent to the {@link Transaction} and the underlying diameter stack and
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
         * transaction was successful or not (e.g. it did receive a 2xxxx answer, or it
         * timed out, or a network error occurred and as such, the original request wasn't even sent etc etc,
         * no matter what, eventually the transaction will have to terminate)
         */
        Builder onTransactionTerminated(Consumer<Transaction> f);

        /**
         * Called once the {@link Transaction} receives any answer and as such, the transaction completes.
         * <p>
         * Note that any {@link DiameterAnswer} received for this transaction is considered to complete
         * the transaction successfully. No interpretation to whether it is a <code>2xxxx</code> v.s. some
         * other answer status family is considered. It merely means that we did receive an answer
         * to the request in a timely fashion.
         * <p>
         * TODO: or do we actually want to that? Perhaps have a on2xxxx on3xxxx etc etc?
         * TODO: perhaps a generic onAnswer and then if the user does a on2xxxx that takes precedence
         * <p>
         * TODO: perhaps have a withGuard type of semantics? That way we are consistent with other parts
         * TODO: of the framework too and we do not make assumptions about what on2xxxx people want to build
         * TODO: and leave it up to them. so:
         * TODO: onAnswer(answer -> true).consume(...)
         *
         * @param f the callback method that will be called upon successful completion
         *          of the {@link Transaction}. The function will take in the {@link Transaction}, which is
         *          this transaction (it's just really for easy access) and the {@link DiameterAnswer} that
         *          completed the transaction.
         * @return
         */
        Builder onAnswer(BiConsumer<Transaction, DiameterAnswer> f);

        /*
        Builder onAnswer(BiConsumer<Transaction, DiameterAnswer> f);
        Builder on2xxx(BiConsumer<Transaction, DiameterAnswer> f);
        Builder on3xxx(BiConsumer<Transaction, DiameterAnswer> f);
        Builder on4xxx(BiConsumer<Transaction, DiameterAnswer> f);
        Builder on5xxx(BiConsumer<Transaction, DiameterAnswer> f);
        Builder on6xxx(BiConsumer<Transaction, DiameterAnswer> f);
         */

        /**
         * Register a function to be called whenever a re-transmission occurs. If no function
         * has been registered, the diameter stack assumes the appliation simply isn't interested
         * and as such, any re-transmissions will silently be handed by the underlying stack.
         * <p>
         * TODO: should perhaps the message that was re-transmitted be part of this? After all, we could
         * be the one re-transmitting the request or we could be the ones that received a re-transmitted
         * request and re-sent the answer and the application wouldn't necessarily know the difference
         * unless we tell them.
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
