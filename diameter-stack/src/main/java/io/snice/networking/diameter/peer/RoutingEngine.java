package io.snice.networking.diameter.peer;

import io.snice.codecs.codec.diameter.DiameterMessage;

import java.util.Optional;

public interface RoutingEngine {

    Optional<Peer> findRoute(PeerTable peers, DiameterMessage msg);
}
