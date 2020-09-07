package io.snice.networking.diameter.peer.impl;

import io.snice.codecs.codec.diameter.DiameterAnswer;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.codecs.codec.diameter.TransactionIdentifier;
import io.snice.networking.common.Transport;
import io.snice.networking.diameter.PeerConnection;
import io.snice.networking.diameter.event.DiameterMessageEvent;
import io.snice.networking.diameter.event.DiameterMessageWriteEvent;
import io.snice.networking.diameter.peer.Peer;
import io.snice.networking.diameter.peer.PeerId;
import io.snice.networking.diameter.peer.PeerIllegalStateException;
import io.snice.networking.diameter.peer.PeerSettings;
import io.snice.networking.diameter.tx.Transaction;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
    public void send(final DiameterMessage msg) {
        ensureConnection().thenAccept(c -> c.send(msg));
    }

    private void send(final DiameterMessageEvent evt) {
        ensureConnection().thenAccept(c -> c.send(evt));
    }

    @Override
    public Transaction.Builder createNewTransaction(final DiameterRequest.Builder msg) throws PeerIllegalStateException {
        return null;
    }

    @Override
    public Transaction.Builder createNewTransaction(final DiameterRequest req) throws PeerIllegalStateException {
        assertNotNull(req, "The Diameter request cannot be null");
        return new DefaultTransaction.DefaultBuilder(this, req);
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

    private static class DefaultTransaction implements Transaction {

        private final DefaultPeer peer;
        private final DiameterRequest req;
        private final TransactionIdentifier id;
        private final Optional<Object> appData;

        private DefaultTransaction(final DefaultPeer peer, final DiameterRequest req, final Optional<Object> appData) {
            this.peer = peer;
            this.req = req;
            this.appData = appData;
            this.id = TransactionIdentifier.from(req);
        }

        @Override
        public TransactionIdentifier getId() {
            return id;
        }

        @Override
        public DiameterRequest getRequest() {
            return req;
        }

        @Override
        public Optional<Object> getApplicationData() {
            return appData;
        }

        private static class DefaultBuilder implements Transaction.Builder {

            private final DefaultPeer peer;
            private final DiameterRequest req;
            private final DiameterRequest.Builder builder;
            private Object appData;

            private DefaultBuilder(final DefaultPeer peer, final DiameterRequest req) {
                this.peer = peer;
                this.req = req;
                this.builder = null;
            }

            private DefaultBuilder(final DefaultPeer peer, final DiameterRequest.Builder builder) {
                this.peer = peer;
                this.req = null;
                this.builder = builder;
            }

            @Override
            public Transaction.Builder withApplicationData(final Object data) {
                this.appData = data;
                return this;
            }

            @Override
            public Transaction.Builder onTransactionTerminated(final Consumer<Transaction> f) {
                return this;
            }

            @Override
            public Transaction.Builder onAnswer(final BiConsumer<Transaction, DiameterAnswer> f) {
                return this;
            }

            @Override
            public Transaction.Builder onRetransmission(final Consumer<Transaction> f) {
                return this;
            }

            @Override
            public Transaction.Builder onTransactionTimeout(final Consumer<Transaction> f) {
                return this;
            }

            @Override
            public Transaction start() {
                final var t = new DefaultTransaction(peer, req, Optional.ofNullable(appData));
                final var evt = DiameterMessageWriteEvent.of(t);
                peer.send(evt);
                return t;
            }
        }
    }
}
