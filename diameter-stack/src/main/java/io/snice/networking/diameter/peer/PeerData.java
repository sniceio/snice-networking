package io.snice.networking.diameter.peer;

import io.hektor.fsm.Data;
import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.TransactionIdentifier;

import java.util.HashMap;
import java.util.Map;

import static io.snice.preconditions.PreConditions.assertNull;

public class PeerData implements Data {

    private final Map<TransactionIdentifier, DiameterMessage> oustandingTransactions;
    private final PeerConfiguration config;

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


}
