package io.snice.networking.diameter.peer;

import io.hektor.fsm.Data;
import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.TransactionIdentifier;
import io.snice.preconditions.PreConditions;

import java.util.HashMap;
import java.util.Map;

public class PeerData implements Data {

    private final Map<TransactionIdentifier, DiameterMessage> oustandingTransactions;
    private final PeerConfiguration config;

    public PeerData(final PeerConfiguration config) {
        this.config = config;
        oustandingTransactions = new HashMap<>(config.getPeerTransactionTableInitialSize(), 0.75f);
    }

    public boolean hasOutstandingTransaction(final DiameterMessage msg) {
        return hasOutstandingTransaction(TransactionIdentifier.from(msg));
    }

    public boolean hasOutstandingTransaction(final TransactionIdentifier id) {
        return oustandingTransactions.containsKey(id);
    }

    public void storeTransaction(final DiameterMessage msg) {
        final var previous = oustandingTransactions.put(TransactionIdentifier.from(msg), msg);
        // TODO: need to handle this in a better way. Also need to check
        // with the application id as a precaution for phishing.
        PreConditions.assertNull(previous, "We overwrote a previous transaction. Something is wrong.");
    }


}
