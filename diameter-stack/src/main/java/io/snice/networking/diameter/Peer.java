package io.snice.networking.diameter;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.avp.api.OriginHost;
import io.snice.codecs.codec.diameter.avp.api.OriginRealm;
import io.snice.networking.common.Connection;
import io.snice.networking.diameter.peer.impl.PeerConnectionWrapper;

public interface Peer extends Connection<DiameterMessage> {

    static Peer of(Connection<DiameterMessage> actualConnection) {
        return PeerConnectionWrapper.of(actualConnection);
    }

    OriginHost getOriginHost();

    /**
     * Ask the {@link Peer} to finish building the message and then send
     * the result. The {@link Peer} will add its configured {@link OriginHost},
     * {@link OriginRealm} to the builder and then build the message.
     *
     * If you, for whatever reason, do not want this behavior then simply build
     * the message yourself before sending it, which then will go out untouched
     * by the peer.
     *
     * @param msg
     */
    void send(DiameterMessage.Builder msg);
}
