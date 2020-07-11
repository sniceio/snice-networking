package io.snice.networking.diameter.peer;

import io.hektor.fsm.Data;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.TransactionIdentifier;
import io.snice.networking.common.event.ConnectionAttemptCompletedIOEvent;
import io.snice.networking.diameter.Peer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNull;

public class PeerData implements Data {

    private final Map<TransactionIdentifier, DiameterMessage> oustandingTransactions;
    private final PeerConfiguration config;

    private ConnectionAttemptCompletedIOEvent event;

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

    public void storeTransaction(final DiameterMessage msg) {
        final var id = TransactionIdentifier.from(msg);
        final var previous = oustandingTransactions.put(id, msg);
        // TODO: need to handle this in a better way. Also need to check
        // with the application id as a precaution for phishing.
        assertNull(previous, "We overwrote a previous transaction. Something is wrong.");
    }

    /**
     * When a user asks to open a new connection we will attempt to create a {@link Peer}
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
        if (event == null) {
            return Optional.empty();
        }

        final var evt = Optional.of(event);
        event = null;
        return evt;
    }



}
