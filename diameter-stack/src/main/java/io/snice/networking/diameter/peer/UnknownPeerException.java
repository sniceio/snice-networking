package io.snice.networking.diameter.peer;

public class UnknownPeerException extends PeerException {

    public UnknownPeerException(final Peer peer) {
        super(peer, "Unknown Peer \"" + peer.getName() + "\"");
    }
}
