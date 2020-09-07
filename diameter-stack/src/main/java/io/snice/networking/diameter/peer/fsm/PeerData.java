package io.snice.networking.diameter.peer.fsm;

import io.hektor.fsm.Data;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.codecs.codec.diameter.TransactionIdentifier;
import io.snice.networking.common.event.ConnectionActiveIOEvent;
import io.snice.networking.common.event.ConnectionAttemptCompletedIOEvent;
import io.snice.networking.diameter.PeerConnection;
import io.snice.networking.diameter.peer.PeerConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNull;

public class PeerData implements Data {

    private final Map<TransactionIdentifier, InternalTransaction> oustandingTransactions;
    private final PeerConfiguration config;

    private ConnectionAttemptCompletedIOEvent event;
    private ConnectionActiveIOEvent activeEvent;

    public PeerData(final PeerConfiguration config) {
        this.config = config;
        oustandingTransactions = new HashMap<>(PeerConfiguration.getPeerTransactionTableInitialSize(), 0.75f);
    }

    public boolean hasOutstandingTransaction(final DiameterMessage msg) {
        return hasOutstandingTransaction(TransactionIdentifier.from(msg));
    }

    public boolean hasOutstandingTransaction(final TransactionIdentifier id) {
        return oustandingTransactions.containsKey(id);
    }

    /**
     * Store a new transaction. Note that the FSM should already have checked that this isn't a
     * re-transmission.
     *
     * @param req
     * @param isClientTransaction whether or not we are the ones initiating the transaction, meaning, are we
     *                            the ones sending the request (then we are a client) or are we the ones who
     *                            received the request and as such, is processing it as a server.
     * @return
     */
    public InternalTransaction storeTransaction(final DiameterRequest req, final boolean isClientTransaction) {
        final var transaction = InternalTransaction.create(req, isClientTransaction);
        final var previous = oustandingTransactions.put(transaction.getId(), transaction);
        // TODO: need to handle this in a better way. Also need to check
        // with the application id as a precaution for phishing.
        assertNull(previous, "We overwrote a previous transaction. Something is wrong.");
        return transaction;
    }

    public InternalTransaction getTransaction(final DiameterMessage msg) {
        return oustandingTransactions.get(TransactionIdentifier.from(msg));
    }

    /**
     * For incoming connections, we need to store away the event stating that the underlying e.g.
     * TCP or SCTP connection was established since we need to make sure that the Peer
     * is correctly established first. So, hold onto this event.
     */
    public void storeConnectionActiveIoEvent(final ConnectionActiveIOEvent event) {
        this.activeEvent = event;
    }

    public Optional<ConnectionActiveIOEvent> consumeConnectionActiveEvent() {
        final var evt = Optional.ofNullable(activeEvent);
        activeEvent = null;
        return evt;
    }

    /**
     * When a user asks to open a new connection we will attempt to create a {@link PeerConnection}
     * (this is diameter after all) and even though we may e.g. manage to establish the underlying
     * transport channel (tcp, sctp etc) we may fail in the Capability Exchange negotiation.
     * Therefore, we cannot actually claim that the connection was successfully made until
     * the underlying peer FSM is happy. Since the Snice Networking Stack doesn't know
     * about this for diameter, we must catch and hold onto the actual event and only
     * propagate it once we have successfully established the Peer ala Diameter rules.
     */
    public void storeConnectionAttemptEvent(final ConnectionAttemptCompletedIOEvent event) {
        this.event = event;
    }

    public Optional<ConnectionAttemptCompletedIOEvent> consumeConnectionAttemptEvent() {
        final var evt = Optional.ofNullable(event);
        event = null;
        return evt;
    }

}
