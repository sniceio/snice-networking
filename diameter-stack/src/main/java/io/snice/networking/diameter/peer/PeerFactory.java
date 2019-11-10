package io.snice.networking.diameter.peer;

import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.common.Connection;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.diameter.peer.impl.DefaultPeerFactory;

public interface PeerFactory extends FsmFactory<DiameterMessage, PeerState, PeerContext, PeerData> {

    static PeerFactory createDefault() {
        return new DefaultPeerFactory();
    }
}
