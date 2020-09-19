package io.snice.networking.diameter;

import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.app.impl.GenericBootstrap;
import io.snice.networking.diameter.event.DiameterEvent;
import io.snice.networking.diameter.event.DiameterMessageReadEvent;
import io.snice.networking.diameter.tx.Transaction;

import java.util.function.Consumer;

/**
 * The purpose of the {@link DiameterApplication} is to "tighten" the generic {@link NetworkApplication} up a bit
 * and to put it all in a diameter context. E.g., an application that wish to communicate using diameter can use
 * the "raw" {@link NetworkApplication} directly but would have to know about the {@link DiameterBundle}, how to
 * "configure" the {@link NetworkApplication} with the correct {@link DiameterEnvironment} etc etc. This
 * class simply does that for the user and also offers some additional features not provided by the base
 * {@link NetworkApplication}.
 * <p>
 * Additional features implemented by this class:
 * <ul>
 *     <li>Adding a diameter {@link Transaction} concept allowing diameter applications to specify
 *     timeouts etc per request/answer if they so wish. If the user doesn't care, then there is no
 *     requirement that all has to go within a {@link Transaction}</li>
 *     <li>Ability to "arm" a {@link Transaction} with a separate onXXXX callbacks. If a request/answer is
 *     processed within a {@link Transaction} she can optionally specify callbacks for just that transaction.
 *     If a transaction has a callback for e.g. an answer, then the generic matching logic as setup during
 *     the {@link NetworkApplication#initialize(NetworkBootstrap)} will be bypassed.</li>
 *     <li></li>
 *     <li></li>
 * </ul>
 *
 * @param <C>
 */
public abstract class DiameterApplication<C extends DiameterAppConfig> extends NetworkApplication<DiameterEnvironment<C>, PeerConnection, DiameterEvent, C> {

    public DiameterApplication() {
        super(new DiameterBundle<>());
    }

    /**
     * In order to be in full control, the {@link DiameterApplication} will not allow the application built ontop
     * of it to use this method directly but the application rather has to override
     * {@link #initialize(DiameterBootstrap)}. This so that the {@link DiameterApplication} can insert itself and
     * allow the application to use specific callbacks per {@link Transaction}.
     *
     * Note, for certain things, such as dropping an incoming connection, we will simply just pass that directly
     * back to the "regular" bootstrap provided by the {@link NetworkApplication}.
     *
     * @param bootstrap
     */
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

    /**
     * This is our "trap" function for all connections that were accepted by the user application. In short, if the
     * user applicaiton wish to accept the incoming connection we will push all events through this function
     * and if the event is an event that may contain a diameter {@link Transaction} (not all of them can)
     * then we first check if the user did create a {@link Transaction} for this request/answer exchange and if so,
     * if they also specified a callback for the particular event
     * (onXXXX such as {@link Transaction.Builder#onRetransmission(Consumer)}) and if they did, we will honor that
     * callback as opposed to the "regular" context rules.
     */
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
        // TODO: specified, do we fallback to the general context?
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
