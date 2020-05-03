package io.snice.networking.app;

import io.snice.buffer.Buffer;
import io.snice.networking.app.impl.NettyNetworkStack;
import io.snice.codecs.codec.SerializationFactory;
import io.snice.networking.common.Connection;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletionStage;

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
        ConfigurationStep<T> withSerializationFactory(SerializationFactory<T> serializationFactory);
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
     * Connect to a remote address using the supplied {@link Transport}.
     *
     * @param remoteAddress
     * @param transport
     * @return a {@link CompletionStage} that, once completed, will contain the {@link } that
     *         is connected to the remote address.
     * @throws IllegalTransportException in case the underlying {@link NetworkStack} isn't configured with
     *         the specified {@link Transport}
     */
    CompletionStage<Connection<T>> connect(Transport transport, InetSocketAddress remoteAddress)
            throws IllegalTransportException;

    /**
     * Shutdown the stack. This method is blocking and will return once
     * the entire stack has been shutdown, which means to stop listening
     * on all configured ports.
     */
    void stop();

    interface Builder<T, C extends NetworkAppConfig> {
        Builder<T, C> withApplication(NetworkApplication<T, C> application);
        Builder<T, C> withAppBundle(AppBundle<T> bundle);
        Builder<T, C> withConnectionContexts(List<ConnectionContext> ctxs);
        NetworkStack<T, C> build();
    }
}
