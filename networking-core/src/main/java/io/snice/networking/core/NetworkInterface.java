package io.snice.networking.core;

import io.snice.networking.common.Connection;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

/**
 * @author jonas@jonasborjesson.com
 */
public interface NetworkInterface<T> {

    /**
     * Get the friendly name of this interface.
     *
     * @return
     */
    String getName();

    /**
     * Bring this interface up, as in start listening to its dedicated listening points.
     * @return a future that when completed guarantees
     * that all listening points were successfully setup.
     */
    CompletionStage<Void> up();

    CompletionStage<Void> down();

    /**
     * Use this {@link NetworkInterface} to connect to a remote address using the supplied
     * {@link Transport}.
     *
     * Note, if the {@link Transport} is a connection less transport, such as UDP, then there isn't
     * a "connect" per se.
     *
     * @param remoteAddress
     * @param transport
     * @return a {@link Future} that, once completed, will contain the {@link } that
     *         is connected to the remote address.
     * @throws IllegalTransportException in case the {@link NetworkInterface} isn't configured with
     *         the specified {@link Transport}
     */
    CompletionStage<Connection<T>> connect(Transport transport, InetSocketAddress remoteAddress)
            throws IllegalTransportException;

    ListeningPoint getListeningPoint(Transport transport);
}
