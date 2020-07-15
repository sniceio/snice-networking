package io.snice.networking.app;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.SerializationFactory;
import io.snice.networking.app.impl.NettyNetworkStack;
import io.snice.networking.common.Connection;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface NetworkStack<K extends Connection<T>, T, C extends NetworkAppConfig> {

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
    static <T> ConnectionTypeStep<T> ofType(final Class<T> type) {
        return NettyNetworkStack.ofType(type);
    }

    interface ConnectionTypeStep<T> {
        <K extends Connection<T>> ConfigurationStep<K, T> withConnectionType(Class<K> type);
    }

    static <K extends Connection<T>, T, C extends NetworkAppConfig> Builder<K, T, C> withConfiguration(C config) {
        return NettyNetworkStack.ofConfiguration(config);
    }

    interface ConfigurationStep<K extends Connection<T>, T> {
        <C extends NetworkAppConfig> Builder<K, T, C> withConfiguration(C config);
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

    interface Builder<K extends Connection<T>, T, C extends NetworkAppConfig> {
        Builder<K, T, C> withApplication(NetworkApplication<K, T, C> application);
        Builder<K, T, C> withAppBundle(AppBundle<K, T> bundle);
        Builder<K, T, C> withConnectionContexts(List<ConnectionContext> ctxs);
        NetworkStack<K, T, C> build();
    }

}
