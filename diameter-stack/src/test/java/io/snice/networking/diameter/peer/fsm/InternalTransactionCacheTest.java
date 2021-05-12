package io.snice.networking.diameter.peer.fsm;

import io.snice.codecs.codec.diameter.DiameterHeader;
import io.snice.codecs.codec.diameter.DiameterRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InternalTransactionCacheTest {

    private static final int INITIAL_SIZE = 1;
    private static final int CACHE_SIZE = 3;
    private static final int EXPIRY_INTERVAL_IN_SECONDS = 3;

    private DiameterRequest request;
    private InternalTransaction transaction;
    private InternalTransactionCache requestCache;

    @Before
    public void setUp() {
        request = mockRequest();
        transaction = createTransaction(request);
        final var cacheConfig = InternalTransactionCacheConfig.of(EXPIRY_INTERVAL_IN_SECONDS)
                .withInitialSize(INITIAL_SIZE)
                .withMaxEntries(CACHE_SIZE)
                .build();
        requestCache = new InternalTransactionCache(cacheConfig);
    }

    private DiameterRequest mockRequest() {
        final var request = mock(DiameterRequest.class);
        when(request.getHeader()).thenReturn(DiameterHeader.createAIR().build());
        return request;
    }

    private InternalTransaction createTransaction(final DiameterRequest request) {
        return InternalTransaction.create(request, true, EXPIRY_INTERVAL_IN_SECONDS);
    }

    @Test
    public void testNewEntry() {
        assertNull(requestCache.putIfAbsent(transaction.getId(), transaction));
    }

    @Test
    public void testExistingEntry() {
        assertNull(requestCache.putIfAbsent(transaction.getId(), transaction));

        final var existing = requestCache.putIfAbsent(transaction.getId(), transaction);
        assertFalse(existing.isExpired());
        assertEquals(transaction.getTransaction(), existing.getTransaction());
        assertEquals(transaction.getId(), existing.getId());
        assertEquals(transaction.isClientTransaction(), existing.isClientTransaction());
    }

    @Test
    public void testExpiry() throws Exception {
        assertNull(requestCache.putIfAbsent(transaction.getId(), transaction));

        // Sleep for slightly more than the expiry interval
        Thread.sleep((EXPIRY_INTERVAL_IN_SECONDS * 1000) + 200);
        final var existing = requestCache.putIfAbsent(transaction.getId(), transaction);
        assertNull(existing);
    }

    @Test
    public void testReplacementOnCacheFull() {
        final var requests = new DiameterRequest[CACHE_SIZE];
        int i;
        for (i = 0; i < CACHE_SIZE; i++) {
            requests[i] = mockRequest();
            final var t = createTransaction(requests[i]);
            requestCache.putIfAbsent(t.getId(), t);
        }
        assertTrue(requestCache.containsEntryForRequest(requests[0]));

        // the next entry should push out the earliest inserted entry
        final var newRequest = mockRequest();
        final var newTransaction = createTransaction(newRequest);
        assertNull(requestCache.putIfAbsent(newTransaction.getId(), newTransaction));
        assertFalse(requestCache.containsEntryForRequest(requests[0]));
    }
}
