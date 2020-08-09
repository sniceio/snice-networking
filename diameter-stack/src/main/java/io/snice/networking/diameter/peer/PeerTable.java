package io.snice.networking.diameter.peer;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.diameter.DiameterAppConfig;
import io.snice.networking.diameter.DiameterConfig;
import io.snice.networking.diameter.DiameterRoutingException;
import io.snice.networking.diameter.PeerConnection;
import io.snice.networking.diameter.event.DiameterEvent;
import io.snice.networking.diameter.peer.impl.DefaultPeerTable;

import java.util.List;
import java.util.concurrent.CompletionStage;

import static io.snice.preconditions.PreConditions.ensureNotNull;

public interface PeerTable<C extends DiameterAppConfig> extends FsmFactory<DiameterEvent, PeerState, PeerContext, PeerData> {

    /**
     * Create a new {@link PeerTable} based on the {@link DiameterConfig} settings.
     */
    static PeerTable create(final DiameterConfig conf, final RoutingEngine routingEngine) {
        ensureNotNull(conf, "The configuration object cannot be null");
        return new DefaultPeerTable(conf, routingEngine);
    }

    CompletionStage<PeerTable<C>> start(final NetworkStack<PeerConnection, DiameterEvent, C> stack);

    /**
     * Ask the {@link PeerTable} to send the given message.
     *
     * @param msg
     * @throws DiameterRoutingException in case the message cannot be sent for any routing reasons, such as
     * unable to find an appropriate {@link Peer} to send the message over.
     */
    void send(DiameterMessage msg) throws DiameterRoutingException;

    /**
     * Get all available {@link Peer}s, which is the complete list of all known peers, irrespective
     * of if they are currently connected to their remote party or not.
     *
     * Note: the list is a snapshot of the state right now and as {@link Peer}s is added/removed, it will
     * NOT be reflected in this list. The list is immutable. As such, no applications should really rely
     * on this list.
     */
    List<Peer> getPeers();

    /**
     * Add a {@link Peer} to the {@link PeerTable} based on the given
     * configuration. If the configuration indicates that {@link Peer}
     * is active and the diameter stack is already running, the stack will make
     * and attempt to actually establish the underlying {@link PeerConnection} as well.
     * <p>
     * If the peer is marked as passive, or the diameter stack is currently not running,
     * the peer will just be added to the internal table.
     *
     * @param config
     * @return {@link Peer}
     */
    Peer addPeer(PeerConfiguration config);

    /**
     * Remove the {@link Peer} from this {@link PeerTable}. If the {@link Peer} has an active
     * connection with its remote party (a {@link PeerConnection}, that connection will be shut down. If the {@link Peer}
     * has any outstanding transactions, they may be allowed to finish first depending on whether
     * the 'now' flag is set or not
     *
     * @param peer the actual peer to remove and shut down.
     * @param now if true the {@link PeerConnection} will forcibly be shutdown
     *            irrespective if the {@link Peer} has any outstanding transactions or not. If false, it is
     *            a "nice" shutdown that will allow the peer to cleanup before going down.
     * @return once completed, the {@link CompletionStage} will contain a reference to the {@link Peer} that
     * was just shutdown, which will be the same peer as passed into the method (note, may not be the same reference
     * so do not not not rely on that).
     */
    CompletionStage<Peer> removePeer(Peer peer, boolean now);

}
