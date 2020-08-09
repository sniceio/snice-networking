package io.snice.networking.diameter.peer.impl;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;
import io.snice.networking.diameter.DiameterAppConfig;
import io.snice.networking.diameter.DiameterEnvironment;
import io.snice.networking.diameter.PeerConnection;
import io.snice.networking.diameter.event.DiameterEvent;
import io.snice.networking.diameter.peer.Peer;
import io.snice.networking.diameter.peer.PeerConfiguration;
import io.snice.networking.diameter.peer.PeerTable;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class DefaultDiameterEnvironment<C extends DiameterAppConfig> implements DiameterEnvironment<C> {

    private final NetworkStack<PeerConnection, DiameterEvent, C> stack;
    private final C config;
    private final PeerTable peerTable;

    public DefaultDiameterEnvironment(final NetworkStack<PeerConnection, DiameterEvent, C> stack,
                                      final PeerTable peerTable, final C config) {
        this.stack = stack;
        this.config = config;
        this.peerTable = peerTable;
    }

    @Override
    public C getConfig() {
        return config;
    }

    /**
     * Ask the diameter stack to send the given message and allow the stack to pick the
     * appropriate {@link PeerConnection} to use.
     *
     * @param msg the message to send.
     */
    @Override
    public void send(final DiameterMessage msg) {
        peerTable.send(msg);
    }

    @Override
    public Peer addPeer(final PeerConfiguration config) {
        return peerTable.addPeer(config);
    }

    /**
     * Get all peers that the underlying diameter stack is managing.
     * <p>
     * Note: some of these may not currently be connected.
     *
     * @return a list of all known {@link PeerConnection}s, irrespective of their status
     */
    @Override
    public List<Peer> getPeers() {
        return peerTable.getPeers();
    }

    @Override
    public CompletionStage<PeerConnection> connect(final Transport transport, final InetSocketAddress remoteAddress) throws IllegalTransportException {
        return stack.connect(transport, remoteAddress).thenApply(PeerConnection::of);
    }
}
