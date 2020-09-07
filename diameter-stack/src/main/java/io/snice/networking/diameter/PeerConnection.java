package io.snice.networking.diameter;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.avp.api.OriginHost;
import io.snice.codecs.codec.diameter.avp.api.OriginRealm;
import io.snice.networking.common.Connection;
import io.snice.networking.diameter.event.DiameterEvent;
import io.snice.networking.diameter.event.DiameterMessageEvent;
import io.snice.networking.diameter.peer.Peer;
import io.snice.networking.diameter.peer.impl.PeerConnectionWrapper;

public interface PeerConnection extends Connection<DiameterEvent> {

    static PeerConnection of(final Connection<DiameterEvent> actualConnection) {
        return PeerConnectionWrapper.of(actualConnection);
    }

    OriginHost getOriginHost();

    /**
     * Ask the {@link PeerConnection} to finish building the message and then send
     * the result. The {@link PeerConnection} will add its configured {@link OriginHost},
     * {@link OriginRealm} to the builder and then build the message.
     * <p>
     * If you, for whatever reason, do not want this behavior then simply build
     * the message yourself before sending it, which then will go out untouched
     * by the peer.
     * <p>
     * If the {@link PeerConnection} is currently not actually established with the remote party
     * the message will be buffered and sent out as soon as a successful connection
     * has been made.
     *
     * TODO: perhaps return a CompletionState with a Transaction in it? Perhaps an Either?
     *
     * @param msg
     */
    void send(DiameterMessage.Builder msg);

    void send(DiameterMessage msg);

    /**
     * Get the {@link Peer} associated with this connection. At the end of the day, the connection
     * is just that, a transport connection (tcp, sctp, udp, tls etc). However, in Diameter, the {@link Peer}
     * represents a higher-level concept of the underlying connection.
     */
    Peer getPeer();

}
