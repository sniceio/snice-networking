package io.snice.networking.diameter;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.avp.api.OriginHost;
import io.snice.codecs.codec.diameter.avp.api.OriginRealm;
import io.snice.networking.common.Connection;
import io.snice.networking.diameter.peer.PeerConfiguration;
import io.snice.networking.diameter.peer.impl.PeerConnectionWrapper;

public interface Peer extends Connection<DiameterMessage> {

    /**
     * A {@link Peer} can either be active or passive. If a peer is active it means that it will
     * try and reach out to the remote end by initiate the connection and initiate the
     * Capability Exchange with that remote peer. In passive mode, it will simply wait
     * and listen for any incoming connections and if configured to do so, will accept
     * that remote peer.
     */
    enum MODE {
        ACTIVE, PASSIVE
    }

    static Peer of(final Connection<DiameterMessage> actualConnection) {
        return PeerConnectionWrapper.of(actualConnection);
    }

    OriginHost getOriginHost();

    /**
     * Get the configuration for this peer.
     *
     * @return
     */
    PeerConfiguration getConfiguration();

    MODE getMode();

    /**
     * Ask the {@link Peer} to finish building the message and then send
     * the result. The {@link Peer} will add its configured {@link OriginHost},
     * {@link OriginRealm} to the builder and then build the message.
     * <p>
     * If you, for whatever reason, do not want this behavior then simply build
     * the message yourself before sending it, which then will go out untouched
     * by the peer.
     * <p>
     * If the {@link Peer} is currently not actually established with the remote party
     * the message will be buffered and sent out as soon as a successful connection
     * has been made.
     *
     * TODO: perhaps return a CompletionState with a Transaction in it? Perhaps an Either?
     *
     * @param msg
     */
    void send(DiameterMessage.Builder msg);

}
