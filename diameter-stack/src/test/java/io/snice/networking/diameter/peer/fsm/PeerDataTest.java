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
public class PeerDataTest {

    private static final int CACHE_SIZE = 3;
    private static final int EXPIRY_INTERVAL_IN_SECONDS = 3;

    private DiameterRequest request;
    private PeerData peerData;

    @Before
    public void setUp() {
        request = mockRequest();
        final var cacheConfig = InternalTransactionCacheConfig.of(EXPIRY_INTERVAL_IN_SECONDS)
                                        .withMaxEntries(CACHE_SIZE)
                                        .build();
        peerData = new PeerData(cacheConfig);
    }

    private DiameterRequest mockRequest() {
        final var request = mock(DiameterRequest.class);
        when(request.getHeader()).thenReturn(DiameterHeader.createAIR().build());
        return request;
    }

    @Test
    public void testExpiry() throws Exception {
        final var transaction = peerData.storeTransaction(request, true);
        final var expectedTransaction = peerData.getTransaction(request);
        assertEquals(expectedTransaction, transaction);

        // Sleep for slightly more than the expiry interval
        Thread.sleep((EXPIRY_INTERVAL_IN_SECONDS * 1000) + 200);
        final var existing = peerData.getTransaction(request);
        assertNull(existing);
    }

    @Test
    public void testReplacementOnCacheFull() {
        final var requests = new DiameterRequest[CACHE_SIZE];
        int i;
        for (i = 0; i < CACHE_SIZE; i++) {
            requests[i] = mockRequest();
            peerData.storeTransaction(requests[i], false);
        }
        assertTrue(peerData.hasOutstandingTransaction(requests[0]));

        // the next entry should push out the earliest inserted entry
        final var newRequest = mockRequest();
        peerData.storeTransaction(newRequest, false);
        assertFalse(peerData.hasOutstandingTransaction(requests[0]));
    }

}