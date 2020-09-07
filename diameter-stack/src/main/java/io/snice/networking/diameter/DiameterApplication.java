package io.snice.networking.diameter;

import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.app.impl.GenericBootstrap;
import io.snice.networking.diameter.event.DiameterEvent;
import io.snice.networking.diameter.event.DiameterMessageReadEvent;

/**
 * @param <C>
 */
public abstract class DiameterApplication<C extends DiameterAppConfig> extends NetworkApplication<DiameterEnvironment<C>, PeerConnection, DiameterEvent, C> {

    // private List<ConnectionContext<PeerConnection, DiameterEvent>> rules;

    public DiameterApplication() {
        super(new DiameterBundle<>());
    }

    @Override
    public final void initialize(final NetworkBootstrap<PeerConnection, DiameterEvent, C> bootstrap) {
        final var diameterBootstrap = new DiameterBootstrapImpl<>(bootstrap.getConfiguration());
        initialize((DiameterBootstrap) diameterBootstrap);
        diameterBootstrap.getConnectionContexts().forEach(r -> {
            final var rule = r;
            final var b = bootstrap.onConnection(rule.getPredicate());
            if (rule.isDrop()) {
                b.drop(rule.getDropFunction().get());
            } else {
                b.accept(builder -> {
                    builder.match(e -> true).consume((peer, event) -> processEvent(rule, peer, event));
                });
            }
        });
    }

    private void processEvent(final ConnectionContext<PeerConnection, DiameterEvent> ctx, final PeerConnection peer, final DiameterEvent event) {
        if (event.isMessageReadEvent()) {
            processMessageReadEvent(ctx, peer, event.toMessageReadEvent());
        } else {
            ctx.match(peer, event).apply(peer, event);
        }
    }

    private void processMessageReadEvent(final ConnectionContext<PeerConnection, DiameterEvent> ctx, final PeerConnection peer, final DiameterMessageReadEvent event) {
        final var transactionMaybe = event.getTransaction();

        // TODO: this was a hack to try some stuff out. Should allow for a message pipe thing
        // TODO: also, what if a transaction is present but e.g no onAnswer callback has been
        // specified, do we fallback to the general context?
        if (transactionMaybe.isPresent()) {
            final var transaction = transactionMaybe.get();
            if (event.isAnswer()) {
                final var callback = transaction.getOnAnswer();
                if (callback != null) {
                    callback.accept(transaction, event.getAnswer());
                }
            }
        } else {
            ctx.match(peer, event).apply(peer, event);
        }
    }

    /**
     * Diameter applications need to override this initialize method, which has the same purpose as the
     * general {@link NetworkApplication#initialize(NetworkBootstrap)} but is tailored to diameter only.
     */
    public abstract void initialize(final DiameterBootstrap<C> bootstrap);

    private class DiameterBootstrapImpl<C extends DiameterAppConfig> extends GenericBootstrap<PeerConnection, DiameterEvent, C> implements DiameterBootstrap<C> {
        public DiameterBootstrapImpl(final C config) {
            super(config);
        }
    }
}
