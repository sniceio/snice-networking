package io.snice.networking.diameter.peer;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.codecs.codec.diameter.avp.api.OriginHost;
import io.snice.functional.Either;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.diameter.DiameterEnvironment;
import io.snice.networking.diameter.PeerConnection;
import io.snice.networking.diameter.tx.Transaction;

import java.util.concurrent.CompletionStage;

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
    * A {@link Peer} may have been created as a {@link MODE#PASSIVE} peer, which means that no
    * attempt to establish a connection to its remote party has been made. If you wish to ensure that the
    * peer is indeed established, and as such, successfully connected to the remote party, call this method.
    * If the {@link Peer} already was established, calling this method is essentially a no-op and a successful
    * {@link CompletionStage} with a reference to this {@link Peer} will be returned.
    * <p>
    * TODO: perhaps return an {@link Either}
    *
    * @return a {@link CompletionStage} with a reference to this {@link Peer} if a connection to the remote endpoint
    * was made successfully. If establishing the peer fails, this {@link CompletionStage} will fail exceptionally.
    */
   CompletionStage<Peer> establishPeer();

   PeerId getId();

   /**
    * Get the {@link MODE} of this {@link Peer}.
    * <p>
    * Note that if the mode is {@link MODE#PASSIVE} it only means that the {@link Peer} wasn't
    * automatically established when it was created and added to the stack. However, something/someone could
    * have asked to {@link #establishPeer()} at a later point.
    */
   MODE getMode();

   String getName();

   /**
    * Ask the {@link Peer} to finalize building the message by adding mandatory AVPs, such as {@link OriginHost},
    * and then send the final message.
    * <p>
    * If you do not wish to have any AVPs automatically added, you can just use the overload {@link #send(DiameterMessage)}
    * method instead.
    * <p>
    * If the {@link Peer} has not been asked to establish a connection to the remote party, either by setting the mode to
    * {@link MODE#ACTIVE} before adding it to the {@link DiameterEnvironment#addPeer(PeerConfiguration)} or by
    * "manually" calling {@link Peer#establishPeer()}, a {@link PeerIllegalStateException} will be thrown.
    *
    * @throws PeerIllegalStateException in case the {@link Peer} has never made an attempt to be established
    * towards the remote endpoint.
    */
   void send(DiameterMessage.Builder msg) throws PeerIllegalStateException;

   /**
    * Ask the {@link Peer} to send the given message. Since the message has been fully constructed, the peer will
    * not add any new AVPs to the message but rather send it as is.
    * <p>
    * The {@link Transaction} created will be a default {@link Transaction} where all responses are still
    * delivered via the bootstrap rules that were setup during the bootstrap of the application. See
    * {@link io.snice.networking.app.NetworkApplication#initialize(NetworkBootstrap)}
    * <p>
    * If the {@link Peer} has not been asked to establish a connection to the remote party, either by setting the mode to
    * {@link MODE#ACTIVE} before adding it to the {@link DiameterEnvironment#addPeer(PeerConfiguration)} or by
    * "manually" calling {@link Peer#establishPeer()}, a {@link PeerIllegalStateException} will be thrown.
    *
    * @throws PeerIllegalStateException in case the {@link Peer} has never made an attempt to be established
    *                                   towards the remote endpoint.
    */
   void send(DiameterMessage msg) throws PeerIllegalStateException;

   Transaction.Builder createNewTransaction(DiameterRequest.Builder req) throws PeerIllegalStateException;

   Transaction.Builder createNewTransaction(DiameterRequest req) throws PeerIllegalStateException;

}
