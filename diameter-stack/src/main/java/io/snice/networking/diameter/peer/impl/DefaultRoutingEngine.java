package io.snice.networking.diameter.peer.impl;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.diameter.peer.Peer;
import io.snice.networking.diameter.peer.PeerTable;
import io.snice.networking.diameter.peer.RoutingEngine;

import java.util.Optional;

public class DefaultRoutingEngine implements RoutingEngine {

    @Override
    public Optional<Peer> findRoute(final PeerTable peers, final DiameterMessage msg) {
        return Optional.empty();
    }
}
