package io.snice.networking.app;

import io.snice.buffer.Buffer;
import io.snice.networking.app.impl.NettyNetworkStack;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;
import io.snice.networking.core.NetworkInterface;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
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
    /*
    static <T> ConfigurationStep<T> ofType(final Class<T> type) {
        return NettyNetworkStack.ofType(type);
    }
     */

    /*
    interface ConnectionTypeStep<T> {
        <K extends Connection<T>> ConfigurationStep<K, T> withConnectionType(Class<K> type);
    }
     */
    static <E extends Environment<K, T, C>, K extends Connection<T>, T, C extends NetworkAppConfig> Builder<E, K, T, C> withConfiguration(final C config) {
        return NettyNetworkStack.ofConfiguration(config);
    }

    /*
    interface ConfigurationStep<K extends Connection<T>, T> {
        <C extends NetworkAppConfig> Builder<K, T, C> withConfiguration(C config);
    }
     */

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
     * Some protocols may want to convert the {@link Connection} into a specific type of connection
     * and must do so by implementing a {@link Environment} and convert it there. Many stacks may not need
     * this but e.g. Diameter deals with so-called Peers as connection objects and as such, the Diameter
     * applications should interact with a Peer as opposed to a "raw" connection.
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
     * Every Snice Networking stack must have a default network interface configured. If the stack
     * only has a single one configured, that will be default. If there are more than one, one of
     * them will be marked as a default. This is enforced when the stack is starting and if not true,
     * the {@link NetworkStack} will error out and shut down.
     *
     * @return
     */
    NetworkInterface<T> getDefaultNetworkInterface();
    /**
     * Try and locate a given {@link NetworkInterface} by its friendly name and {@link Transport}.
     *
     * @param interfaceName
     * @param transport
     * @return
     */
    Optional<NetworkInterface<T>> getNetworkInterface(String interfaceName, Transport transport);

    /**
     * Try and locate a given {@link NetworkInterface} based on its friendly name.
     *
     * @param interfaceName the name of the interface to look up
     */
    Optional<NetworkInterface<T>> getNetworkInterface(String interfaceName);

    /**
     * Find all {@link NetworkInterface}s that supports the given {@link Transport}
     *
     * @param transport
     * @return a list of {@link NetworkInterface}s that all has support for the given {@link Transport}
     */
    List<NetworkInterface<T>> getNetworkInterfaces(Transport transport);

    /**
     * Shutdown the stack. This method is blocking and will return once
     * the entire stack has been shutdown, which means to stop listening
     * on all configured ports.
     */
    void stop();

    interface Builder<E extends Environment<K, T, C>, K extends Connection<T>, T, C extends NetworkAppConfig> {
        Builder<E, K, T, C> withApplication(NetworkApplication<E, K, T, C> application);

        Builder<E, K, T, C> withAppBundle(ProtocolBundle<K, T, C> bundle);

        Builder<E, K, T, C> withConnectionContexts(List<ConnectionContext<K, T>> ctxs);

        NetworkStack<K, T, C> build();
    }

}
