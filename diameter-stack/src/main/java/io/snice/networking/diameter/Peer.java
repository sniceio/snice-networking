package io.snice.networking.diameter;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.avp.api.OriginHost;
import io.snice.networking.common.Connection;
import io.snice.networking.diameter.peer.impl.PeerConnectionWrapper;

public interface Peer extends Connection<DiameterMessage> {

    static Peer of(Connection<DiameterMessage> actualConnection) {
        return PeerConnectionWrapper.of(actualConnection);
    }

    OriginHost getOriginHost();
}
