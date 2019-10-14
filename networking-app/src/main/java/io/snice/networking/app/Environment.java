package io.snice.networking.app;

import io.snice.networking.common.Connection;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Environment<T, C extends NetworkAppConfig> {

    /**
     * Obtain the loaded configuration.
     */
    C getConfig();

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
    CompletionStage<Connection<C>> connect(Transport transport, InetSocketAddress remoteAddress)
            throws IllegalTransportException;



}
