package io.snice.networking.diameter.peer;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.diameter.Peer;

import java.util.Optional;

public interface RoutingEngine {

    Optional<Peer> findRoute(DiameterMessage msg);
}
