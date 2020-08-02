package io.snice.networking.diameter.peer;

public class PeerUnavailableException extends PeerException {

    public PeerUnavailableException(final Peer peer) {
        super(peer, "Peer is unavailable \"" + peer.getName() + "\"");
    }
}
