package io.snice.networking.diameter.peer.impl;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.common.Transport;
import io.snice.networking.diameter.PeerConnection;
import io.snice.networking.diameter.peer.Peer;
import io.snice.networking.diameter.peer.PeerId;
import io.snice.networking.diameter.peer.PeerIllegalStateException;
import io.snice.networking.diameter.peer.PeerSettings;
import io.snice.networking.diameter.tx.Transaction;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultPeer implements Peer {

    private final DefaultPeerTable peerTable;
    private final PeerSettings settings;
    private final PeerId id;

    private final AtomicReference<CompletionStage<PeerConnection>> connection = new AtomicReference<>();

    public static DefaultPeer of(final DefaultPeerTable peerTable, final PeerSettings settings) {
        assertNotNull(peerTable, "The Peer Table cannot be null");
        assertNotNull(settings, "The Peer settings cannot be null");
        final var id = new PeerIdImpl(UUID.randomUUID());
        return new DefaultPeer(id, peerTable, settings);
    }

    private DefaultPeer(final PeerId id, final DefaultPeerTable peerTable, final PeerSettings settings) {
        this.id = id;
        this.settings = settings;
        this.peerTable = peerTable;
    }

    PeerSettings getSettings() {
        return settings;
    }

    public Transport getTransport() {
        return settings.getTransport();
    }

    CompletionStage<InetSocketAddress> resolveRemoteHost() {
        return settings.getResolver().resolve(settings.getUri());
    }

    @Override
    public CompletionStage<Peer> establishPeer() {
        // this is dumb.
        final var maybe = connection.compareAndExchangeAcquire(null, peerTable.activatePeer(this));
        if (maybe != null) {
            return maybe.thenApply(c -> this);
        }

        return connection.get().thenApply(c -> this);
    }

    @Override
    public PeerId getId() {
        return id;
    }

    @Override
    public MODE getMode() {
        return settings.getMode();
    }

    @Override
    public String getName() {
        return settings.getName();
    }

    @Override
    public void send(final DiameterMessage.Builder msg) {
        ensureConnection().thenAccept(c -> c.send(msg));
    }

    @Override
    public Transaction send(final DiameterMessage msg) {
        ensureConnection().thenAccept(c -> c.send(msg));
        return null;
    }

    @Override
    public String toString() {
        return settings.toString();
    }

    private CompletionStage<PeerConnection> ensureConnection() {
        final var f = connection.get();
        if (f == null) {
            throw new PeerIllegalStateException(this, "Peer has never been established");
        }
        return f;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DefaultPeer that = (DefaultPeer) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static class PeerIdImpl implements PeerId {

        private final UUID uuid;

        private PeerIdImpl(final UUID uuid) {
            this.uuid = uuid;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final PeerIdImpl peerId = (PeerIdImpl) o;
            return uuid.equals(peerId.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid);
        }

        @Override
        public String toString() {
            return uuid.toString();
        }
    }
}
