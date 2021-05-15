package io.snice.networking.diameter.peer.fsm;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.hektor.fsm.Data;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.codecs.codec.diameter.HopByHopIdentifier;
import io.snice.networking.common.event.ConnectionActiveIOEvent;
import io.snice.networking.common.event.ConnectionAttemptCompletedIOEvent;
import io.snice.networking.diameter.PeerConnection;
import static io.snice.preconditions.PreConditions.assertNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PeerData implements Data {

    private final Cache<HopByHopIdentifier, InternalTransaction> oustandingTransactions;

    private ConnectionAttemptCompletedIOEvent event;
    private ConnectionActiveIOEvent activeEvent;

    public PeerData(final InternalTransactionCacheConfig cacheConfig) {
        oustandingTransactions = CacheBuilder.newBuilder()
                                             .maximumSize(cacheConfig.getMaxEntries())
                                             .expireAfterWrite(cacheConfig.getExpiryIntervalInSeconds(), TimeUnit.SECONDS)
                                             .build();
    }

    public boolean hasOutstandingTransaction(final DiameterMessage msg) {
        return hasOutstandingTransaction(HopByHopIdentifier.from(msg));
    }

    public boolean hasOutstandingTransaction(final HopByHopIdentifier id) {
        return oustandingTransactions.asMap().containsKey(id);
    }

    /**
     * Store a new transaction. Note that the FSM should already have checked that this isn't a
     * re-transmission.
     *
     * @param req                 the diameter request for which the entry is to be stored
     * @param isClientTransaction whether or not we are the ones initiating the transaction, meaning, are we
     *                            the ones sending the request (then we are a client) or are we the ones who
     *                            received the request and as such, is processing it as a server.
     * @return the created {@link InternalTransaction} if does not exist yet or the existing transaction
     */
    public InternalTransaction storeTransaction(final DiameterRequest req, final boolean isClientTransaction) {
        final var id = HopByHopIdentifier.from(req);
        final var previous = oustandingTransactions.getIfPresent(id);
        // TODO: need to handle this in a better way. Also need to check
        // with the application id as a precaution for phishing.
        assertNull(previous, "We overwrote a previous transaction. Something is wrong.");

        final var transaction = InternalTransaction.create(req, isClientTransaction);
        oustandingTransactions.put(transaction.getId(), transaction);
        return transaction;
    }

    public InternalTransaction getTransaction(final DiameterMessage msg) {
        return oustandingTransactions.getIfPresent(HopByHopIdentifier.from(msg));
    }

    /**
     * For incoming connections, we need to store away the event stating that the underlying
     * transport (e.g. TCP or SCTP connection) was established since we need to make sure that the Peer
     * is correctly established first. So, hold onto this event and once the Peer FSM is in a state
     * where we are ready to let the application now, we'll fire off the event again.
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
