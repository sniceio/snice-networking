package io.snice.networking.diameter.peer;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.diameter.PeerConnection;

import java.util.Optional;

/**
 * The underlying {@link PeerConnection} only gets created once an attempt to connect to the
 * remote endpoint is done, or the remote endpoint connects to us. If successful, a
 * {@link PeerConnection} will be created and associated with its {@link Peer}. As such,
 * the {@link Peer} only represents the meta-data for an actual {@link PeerConnection}.
 */
public interface Peer {

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

   /**
    * Not until a {@link PeerConnection} is connected to it's remote endpoint it actually gets created.
    * Hence, if this method returns an empty {@link Optional}, the underlying {@link PeerConnection} currently
    * doesn't exist.
    *
    * @return
    */
   Optional<PeerConnection> getPeer();

   PeerId getId();

   void send(DiameterMessage.Builder msg);

   void send(DiameterMessage msg);

}
