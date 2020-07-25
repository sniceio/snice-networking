package io.snice.networking.diameter.peer.impl;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.diameter.PeerConnection;
import io.snice.networking.diameter.peer.Peer;
import io.snice.networking.diameter.peer.PeerConfiguration;
import io.snice.networking.diameter.peer.PeerId;
import io.snice.networking.diameter.peer.PeerSettings;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultPeer implements Peer {

    private final PeerSettings settings;
    private final PeerId id;

    private final AtomicReference<PeerConnection> connection = new AtomicReference<>();

    public static DefaultPeer of(final PeerConfiguration config) {
        final var settings = PeerSettings.from(config);
        final var id = new PeerIdImpl(UUID.randomUUID());
        return new DefaultPeer(id, settings);
    }

    private DefaultPeer(final PeerId id, final PeerSettings settings) {
        this.id = id;
        this.settings = settings;
    }

    void setConnection(final PeerConnection peerConnection) {
        this.connection.set(peerConnection);
    }

    @Override
    public PeerId getId() {
        return id;
    }

    @Override
    public void send(DiameterMessage.Builder msg) {
        System.err.println("need to send or buffer");
    }

    @Override
    public void send(DiameterMessage msg) {
        final var peer = connection.get();
        if (peer != null) {
            System.err.println("Got an active peer, sending!");
            peer.send(msg);
        }
    }

    @Override
    public Optional<PeerConnection> getPeer() {
        return Optional.ofNullable(connection.get());
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
