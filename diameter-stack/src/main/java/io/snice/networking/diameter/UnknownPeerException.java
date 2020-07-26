package io.snice.networking.diameter;

import io.snice.networking.diameter.peer.Peer;

public class UnknownPeerException extends PeerException {

    public UnknownPeerException(final Peer peer) {
        super(peer, "Unknown Peer \"" + peer.getName() + "\"");
    }
}
