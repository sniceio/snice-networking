package io.snice.networking.diameter.peer.impl;

import io.snice.networking.diameter.peer.Peer;
import io.snice.networking.diameter.peer.PeerIllegalStateException;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class DefaultPeerTest extends PeerTestBase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testCreatePeer() {
        assertThat(defaultPeer.getId(), notNullValue());
    }

    /**
     * If a {@link Peer} has never been established then we should not be able to
     * send the message and we should get an exception.
     */
    @Test(expected = PeerIllegalStateException.class)
    public void testSendMessagePeerNeverEstablished() throws PeerIllegalStateException {
        defaultPeer.send(someCer());
    }

    @Test
    public void testSendMessage() throws PeerIllegalStateException {
        defaultPeer.establishPeer();
        final var cer = someCer();
        defaultPeer.send(cer);

        // at the end of the day, we have a mocked PeerConnection to which
        // the message should be sent across so just verify that it got it.
        verify(peerConnection).send(cer);
        verifyNoMoreInteractions(peerConnection);
    }


}