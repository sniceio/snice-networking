package io.snice.networking.diameter;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface DiameterEnvironment<C extends DiameterAppConfig> extends Environment<Peer, DiameterMessage, C> {

    C getConfig();

    /**
     * Ask the diameter stack to send the given message and allow the stack to pick the
     * appropriate {@link Peer} to use.
     *
     * @param msg the message to send.
     */
    void send(final DiameterMessage msg);

    /**
     * Get all peers that the underlying diameter stack is managing.
     *
     * Note: some of these may not currently be connected.
     *
     * @return a list of all known {@link Peer}s, irrespective of their status
     */
    List<Peer> getPeers();

    CompletionStage<Peer> connect(final Transport transport, final InetSocketAddress remoteAddress) throws IllegalTransportException;
}
