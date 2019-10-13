package io.snice.networking.app;

import io.snice.buffer.Buffer;
import io.snice.networking.app.impl.NettyNetworkStack;
import io.snice.networking.codec.FramerFactory;

import java.util.List;
import java.util.concurrent.CompletionStage;

import static io.snice.preconditions.PreConditions.assertNotNull;

public interface NetworkStack<T, C extends NetworkAppConfig> {

    /**
     * A {@link NetworkStack} will only deal with one type of "message" at a time. E.g.,
     * you may build a SIP stack and then it will only handle SIP messages in/out of
     * that stack. If you do want just a "raw" stack then you can create one that
     * just handles {@link Buffer} e.g.
     *
     * @param type the type of messages this stack will handle.
     * @param <T>
     * @return
     */
    static <T> FramerFactoryStep<T> ofType(final Class<T> type) {
        return NettyNetworkStack.ofType(type);
    }

    interface FramerFactoryStep<T> {
        ConfigurationStep<T> withFramerFactory(FramerFactory<T> framerFactory);
    }


    interface ConfigurationStep<D> {
        <C extends NetworkAppConfig> Builder<D, C> withConfiguration(C config);
    }

    /**
     * Start the stack. This will configure the underlying network, bind to all
     * ports for all configured transports and the method will block until everything
     * is up and running.
     * <p>
     *
     * @return
     */
    void start();

    /**
     * Get a sync object to be used for knowing when the stack has been shutdown.
     *
     * @return
     */
    CompletionStage<Void> sync();

    /**
     * Shutdown the stack. This method is blocking and will return once
     * the entire stack has been shutdown, which means to stop listening
     * on all configured ports.
     */
    void stop();

    interface Builder<T, C extends NetworkAppConfig> {
        Builder<T, C> withApplication(NetworkApplication<T, C> application);
        Builder<T, C> withConnectionContexts(List<ConnectionContext> ctxs);
        NetworkStack<T, C> build();
    }
}
