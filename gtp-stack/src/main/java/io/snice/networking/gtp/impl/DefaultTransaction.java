package io.snice.networking.gtp.impl;

import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Response;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.gtp.Transaction;
import io.snice.networking.gtp.TransactionIdentifier;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.event.impl.DefaultGtpMessageEvent;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.snice.preconditions.PreConditions.assertNotNull;
import static io.snice.preconditions.PreConditions.assertNull;

public class DefaultTransaction implements InternalGtp2Transaction {

    public static <C extends GtpAppConfig> Transaction.Builder of(final InternalGtpControlTunnel tunnel,
                                                                  final InternalGtpStack<C> stack,
                                                                  final Gtp2Request request) {
        assertNotNull(tunnel, "The tunnel cannot be null");
        assertNotNull(stack, "The GTP Stack cannot be null");
        assertNotNull(request, "The GTP Request cannot be null");
        return new TransactionBuilder(stack, tunnel, request);
    }

    private final Gtp2Request request;
    private final ConnectionId id;
    private final Optional<Object> appData;
    private final BiConsumer<Transaction, Gtp2Response> onResponse;

    private DefaultTransaction(final Gtp2Request request, final Object appData, final ConnectionId id, final BiConsumer<Transaction, Gtp2Response> onResponse) {
        this.request = request;
        this.appData = Optional.ofNullable(appData);
        this.id = id;
        this.onResponse = onResponse;
    }

    @Override
    public TransactionIdentifier getId() {
        return null;
    }

    @Override
    public Gtp2Request getRequest() {
        return request;
    }

    @Override
    public ConnectionId getConnectionId() {
        return id;
    }

    @Override
    public Optional<Object> getApplicationData() {
        return appData;
    }

    @Override
    public BiConsumer<Transaction, Gtp2Response> getOnResponse() {
        return onResponse;
    }

    public static class TransactionBuilder<C extends GtpAppConfig> implements Transaction.Builder {

        private final InternalGtpStack<C> stack;
        private final InternalGtpControlTunnel tunnel;
        private final Gtp2Request request;
        private Object appData;
        private BiConsumer<Transaction, Gtp2Response> onAnswer;

        private TransactionBuilder(final InternalGtpStack<C> stack, final InternalGtpControlTunnel tunnel, final Gtp2Request request) {
            this.stack = stack;
            this.tunnel = tunnel;
            this.request = request;
        }

        @Override
        public Transaction.Builder withApplicationData(final Object data) {
            this.appData = data;
            return this;
        }

        @Override
        public Transaction.Builder onTransactionTerminated(final Consumer<Transaction> f) {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public Transaction.Builder onAnswer(final BiConsumer<Transaction, Gtp2Response> f) {
            assertNotNull(f);
            assertNull(onAnswer, "You have already specified a onAnswer function. You cannot overwrite it.");
            this.onAnswer = f;
            return this;
        }

        @Override
        public Transaction.Builder onRetransmission(final Consumer<Transaction> f) {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public Transaction.Builder onTransactionTimeout(final Consumer<Transaction> f) {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public Transaction start() {
            final var transaction = new DefaultTransaction(request, appData, tunnel.id(), onAnswer);
            final var evt = DefaultGtpMessageEvent.newWriteEvent(request, transaction);
            stack.send(evt, tunnel);
            return transaction;
        }
    }
}
