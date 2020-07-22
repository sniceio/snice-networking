package io.snice.networking.diameter.peer.impl;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.diameter.Peer;
import io.snice.networking.diameter.peer.RoutingEngine;

import java.util.Optional;

public class DefaultRoutingEngine implements RoutingEngine {

    @Override
    public Optional<Peer> findRoute(final DiameterMessage msg) {
        return Optional.empty();
    }
}
