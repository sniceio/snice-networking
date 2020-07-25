package io.snice.networking.diameter.peer;

import io.snice.networking.diameter.PeerConnection;

/**
 * A {@link PeerConnection}, and its corresponding {@link Peer} is always associated with
 * a stable {@link PeerId}, irrespective of whether or not the underlying {@link PeerConnection}
 * actually exists at the time.
 */
public interface PeerId {
}
