package io.snice.networking.app;

import io.snice.networking.common.Connection;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

import static io.snice.preconditions.PreConditions.assertNotEmpty;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Environment<K extends Connection<T>, T, C extends NetworkAppConfig> {

    /**
     * Obtain the loaded configuration.
     */
    C getConfig();

    /**
     * Connect to a remote address using the supplied {@link Transport}.
     *
     * @param remoteAddress
     * @param transport
     * @return a {@link CompletionStage} that, once completed, will contain the {@link Connection} that
     * is connected to the remote address.
     * @throws IllegalTransportException in case the underlying {@link NetworkStack} isn't configured with
     *                                   the specified {@link Transport}
     */
    CompletionStage<K> connect(Transport transport, InetSocketAddress remoteAddress)
            throws IllegalTransportException;

    /**
     * Convenience method for connecting to a remote address and is the same as calling
     * <p>
     * {@link #connect(Transport, InetSocketAddress)} where the {@link InetSocketAddress} is created
     * as:
     *
     * <code>
     * InetSocketAddress.createUnresolved(remoteHost, remoteIp))
     * </code>
     *
     * @param transport
     * @param remoteHost
     * @param remoteIp
     * @return
     * @throws IllegalTransportException
     * @throws IllegalArgumentException  in case the remote host is null or the empty string or if the remote IP
     *                                   is not within a valid port range.
     */
    default CompletionStage<K> connect(final Transport transport, final String remoteHost, final int remoteIp)
            throws IllegalTransportException, IllegalArgumentException {
        assertNotEmpty(remoteHost, "The remote host cannot be null or the empty string");
        return connect(transport, new InetSocketAddress(remoteHost, remoteIp));
    }


}
