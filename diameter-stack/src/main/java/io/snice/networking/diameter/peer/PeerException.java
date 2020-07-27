package io.snice.networking.diameter.peer;

import java.util.Optional;

/**
 * Base exception for all {@link Peer} related issues.
 */
public class PeerException extends RuntimeException {

    private final Optional<Peer> peer;

    public PeerException(final Peer peer, final String msg) {
        super(msg);
        this.peer = Optional.ofNullable(peer);
    }

    /**
     * If available, return the given {@link Peer} that is the cause of this
     * exception.
     */
    public Optional<Peer> getPeer() {
        return peer;
    }
}
