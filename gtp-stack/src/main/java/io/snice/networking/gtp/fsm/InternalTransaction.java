package io.snice.networking.gtp.fsm;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.GtpRequest;
import io.snice.networking.gtp.Transaction;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * We will always keep track of a transaction but the user building the application
 * on top of us may not care. If a user cares to run a particular request/response
 * "through" a transaction the user can create a {@link io.snice.networking.gtp.Transaction} and configure
 * callbacks etc to be applied to that transaction only.
 */
public class InternalTransaction {

    /**
     * The request that created this transaction.
     */
    private final GtpMessage req;

    /**
     * The actual identifier of this transaction, which is just the sequence number
     * as found in the GTP header.
     * <p>
     * Open Question: the seq no field in GTPv1 is optional so need to read up
     * how to match then. For now, really only focusing on GTPv2
     */
    private final Buffer id;

    /**
     * If we are tracking this as a client transaction, it means we are the
     * ones that sent the {@link GtpRequest}, as opposed to when we
     * receive a request and in that case, we are acting as a server.
     */
    private final boolean isClientTransaction;

    /**
     * If set, the user has asked to keep track of this transaction.
     * The FSM doesn't care too much about it but later when we are to
     * invoke the application again for e.g. an answer, or perhaps a retransmission,
     * then this {@link io.snice.networking.gtp.Transaction} object may contain application callbacks.
     */
    private Optional<Transaction> transaction = Optional.empty();

    public static InternalTransaction create(final GtpMessage req,
                                             final Buffer transactionId,
                                             final boolean isClientTransaction) {
        assertNotNull(req, "The GTP request cannot be null");
        Buffers.assertNotEmpty(transactionId, "The transaction ID cannot be null or the empty Buffer");
        return new InternalTransaction(req, transactionId, isClientTransaction);
    }

    private InternalTransaction(final GtpMessage req, final Buffer transactionId, final boolean isClientTransaction) {
        this.req = req;
        this.id = transactionId;
        this.isClientTransaction = isClientTransaction;
    }

    public Buffer getId() {
        return id;
    }

    public void setTransaction(final Transaction transaction) {
        assertNotNull(transaction, "The transaction cannot be null");
        this.transaction = Optional.of(transaction);
    }

    public Optional<Transaction> getTransaction() {
        return transaction;
    }

    public boolean isClientTransaction() {
        return isClientTransaction;
    }

}
