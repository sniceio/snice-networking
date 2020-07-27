package io.snice.networking.diameter.peer;

public class PeerIllegalStateException extends PeerException {

    public PeerIllegalStateException(Peer peer, String msg) {
        super(peer, msg);
    }
}
