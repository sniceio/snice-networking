package io.snice.networking.diameter.peer;

import io.snice.codecs.codec.diameter.avp.api.ResultCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Testing all aspects of the {@link PeerState#CLOSED} state.
 */
public class PeerFsmCloseTest extends PeerFsmTestBase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Ensure that we can accept a well formed CER and that we generate and send
     * back a CEA. The new state should be {@link PeerState#OPEN}
     */
    @Test
    public void testCer() {
        final var cer = someCer();
        fsm.onEvent(cer);

        final var expectedCEA = someCea(ResultCode.DiameterSuccess2001, LOCAL_PEER_IP_ADDRESS, defaultProductName);
        verify(channelCtx).sendDownstream(expectedCEA);
        verifyNoMoreInteractions(channelCtx);
    }
}