package io.snice.networking.diameter.peer.fsm;

import static io.snice.preconditions.PreConditions.assertArgument;

/**
 * Configuration of the cache used to store outstanding transactions
 */
public class InternalTransactionCacheConfig {

    /**
     * Max number of transactions that the cache can store
     */
    private final int maxEntries;

    /**
     * Expiry interval of a transaction.
     * When it expires, the transaction will eventually be purged from the cache
     */
    private final int expiryIntervalInSeconds;

    private InternalTransactionCacheConfig(final int maxEntries, final int expiryIntervalInSeconds) {
        this.maxEntries = maxEntries;
        this.expiryIntervalInSeconds = expiryIntervalInSeconds;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public int getExpiryIntervalInSeconds() {
        return expiryIntervalInSeconds;
    }

    public static InternalTransactionCacheConfig.Builder of(final int expiryIntervalInSeconds) {
        return new InternalTransactionCacheConfig.Builder(expiryIntervalInSeconds);
    }

    public static class Builder {

        private static final int DEFAULT_MAX_ENTRIES_BASE = 1024;

        private final int expiryIntervalInSeconds;
        private int maxEntries;

        private Builder(final int expiryIntervalInSeconds) {
            assertArgument(expiryIntervalInSeconds > 0, "You must specify the Expiry Interval of a transaction. It cannot be < 0");
            this.expiryIntervalInSeconds = expiryIntervalInSeconds;
        }

        public InternalTransactionCacheConfig.Builder withMaxEntries(final int maxEntries) {
            this.maxEntries = maxEntries;
            return this;
        }

        public InternalTransactionCacheConfig build() {
            if(maxEntries <= 0) {
                maxEntries = DEFAULT_MAX_ENTRIES_BASE * expiryIntervalInSeconds;
            }
            return new InternalTransactionCacheConfig(maxEntries, expiryIntervalInSeconds);
        }
    }
}
