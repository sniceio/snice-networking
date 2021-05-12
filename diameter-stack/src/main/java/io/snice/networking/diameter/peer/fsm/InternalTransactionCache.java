package io.snice.networking.diameter.peer.fsm;

import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.codecs.codec.diameter.HopByHopIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class InternalTransactionCache extends LinkedHashMap<HopByHopIdentifier, InternalTransaction>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalTransactionCache.class);

    private final int maxEntries;

    protected InternalTransactionCache(final InternalTransactionCacheConfig cacheConfig) {
        super(cacheConfig.getInitialSize(), 0.75f);
        this.maxEntries = cacheConfig.getMaxEntries();
    }

    /**
     * Puts an entry into the transaction cache if it is not already present.
     * If the transaction already exists in the cache, the existing value is returned, otherwise a new entry is created, and null is returned
     *
     * @param id          the identifier of the transaction, which is just the hop-by-hop identifier.
     * @param transaction the transaction associated to the specified key
     * @return If the transaction already exists in cache, the value associated with it is returned, otherwise a new entry is created, and null is returned
     */
    public synchronized InternalTransaction putIfAbsent(final HopByHopIdentifier id, final InternalTransaction transaction) {
        // If the entry exists, and is not expired, return the value. If the entry is expired, remove it.
        if (containsKey(id)) {
            final var value = get(id);
            if (!value.isExpired()) {
                // TODO: ideally the method should return the cached answer in case of retransmission.
                // So we should store also the answer in the transaction at some point.
                return value;
            } else {
                remove(id);
            }
        }

        put(id, transaction);
        return null;
    }

    public boolean containsEntryForRequest(final DiameterRequest req) {
        return containsKey(HopByHopIdentifier.from(req));
    }

    /**
     * Method overridden to define custom policy on when to remove the eldest entry automatically after the insertion of a new one.
     */
    @Override
    protected boolean removeEldestEntry(final Map.Entry<HopByHopIdentifier, InternalTransaction> eldest) {
        if (size() > maxEntries) {
            if (!eldest.getValue().isExpired()) {
                LOGGER.info("Removing eldest entry that has not expired yet; if this happens often, you probably need to increase the cache size");
            }
            return true;
        }
        return false;
    }
}
