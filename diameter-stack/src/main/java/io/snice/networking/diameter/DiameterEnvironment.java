package io.snice.networking.diameter;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.app.Environment;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;
import io.snice.networking.diameter.peer.Peer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface DiameterEnvironment<C extends DiameterAppConfig> extends Environment<PeerConnection, DiameterMessage, C> {

    @Override
    C getConfig();

    /**
     * Ask the diameter stack to send the given message and allow the stack to pick the
     * appropriate {@link PeerConnection} to use.
     *
     * @param msg the message to send.
     */
    void send(final DiameterMessage msg);

    /**
     * Get all available {@link Peer}s, which is the complete list of all known peers, irrespective
     * of if they are currently connected to their remote party or not.
     *
     * Note: the list is a snapshot of the state right now and as {@link Peer}s is added/removed, it will
     * NOT be reflected in this list. The list is immutable. As such, no applications should really rely
     * on this list.
     */
    List<Peer> getPeers();

    @Override
    CompletionStage<PeerConnection> connect(final Transport transport, final InetSocketAddress remoteAddress) throws IllegalTransportException;
}
