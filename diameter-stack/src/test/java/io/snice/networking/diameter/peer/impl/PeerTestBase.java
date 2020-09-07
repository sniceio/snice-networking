package io.snice.networking.diameter.peer.impl;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.common.Transport;
import io.snice.networking.core.NetworkInterface;
import io.snice.networking.diameter.DiameterAppConfig;
import io.snice.networking.diameter.PeerConnection;
import io.snice.networking.diameter.peer.DiameterTestBase;
import io.snice.networking.diameter.peer.Peer;
import io.snice.networking.diameter.peer.PeerConfiguration;
import io.snice.networking.diameter.peer.PeerSettings;
import org.junit.Before;
import org.mockito.Mock;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class PeerTestBase extends DiameterTestBase {

    @Mock(lenient = true)
    protected DefaultPeerTable<DiameterAppConfig> peerTable;

    @Mock(lenient = true)
    protected NetworkInterface<DiameterMessage> nic;

    @Mock
    protected PeerConnection peerConnection;

    protected PeerSettings peerSettings;
    protected Peer defaultPeer;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        final var peerConfig = new PeerConfiguration();

        when(nic.getName()).thenReturn("default");

        peerSettings = PeerSettings.of(peerConfig)
                .withTransport(Transport.tcp)
                .withNetworkInterface(nic)
                .build();

        when(peerTable.activatePeer(any())).thenReturn(CompletableFuture.completedFuture(peerConnection));
        defaultPeer = DefaultPeer.of(peerTable, peerSettings);
    }
}