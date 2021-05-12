package io.snice.networking.diameter.peer.fsm;

import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.codecs.codec.diameter.HopByHopIdentifier;
import io.snice.networking.diameter.tx.Transaction;

import java.util.Objects;
import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * We will always keep track of a transaction but the user building the application
 * on top of us may not care. If a user cares to run a particular request/answer
 * "through" a transaction the user can create a {@link Transaction} and configure
 * callbacks etc to be applied to that transaction only.
 */
public class InternalTransaction {

    /**
     * The request that created this transaction.
     */
    private final DiameterRequest req;

    /**
     * The actual identifier of this transaction, which is just the
     * hop-by-hop identifier.
     */
    private final HopByHopIdentifier id;

    /**
     * If we are tracking this as a client transaction, it means we are the
     * ones that sent the {@link DiameterRequest}, as opposed to when we
     * receive a request and in that case, we are acting as a server.
     */
    private final boolean isClientTransaction;

    /**
     * If set, the user has asked to keep track of this transaction.
     * The FSM doesn't care too much about it but later when we are to
     * invoke the application again for e.g. an answer, or perhaps a retransmission,
     * then this {@link Transaction} object may contain application callbacks.
     */
    private Optional<Transaction> transaction = Optional.empty();

    public static InternalTransaction create(final DiameterRequest req, final boolean isClientTransaction) {
        assertNotNull(req, "The diameter request cannot be null");
        return new InternalTransaction(req, isClientTransaction);
    }

    private InternalTransaction(final DiameterRequest req, final boolean isClientTransaction) {
        this.req = req;
        this.id = HopByHopIdentifier.from(req);
        this.isClientTransaction = isClientTransaction;
    }

    public HopByHopIdentifier getId() {
        return id;
    }

    public Optional<Transaction> getTransaction() {
        return transaction;
    }

    public boolean isClientTransaction() {
        return isClientTransaction;
    }

    public void setTransaction(final Transaction transaction) {
        this.transaction = Optional.of(transaction);
    }

}
