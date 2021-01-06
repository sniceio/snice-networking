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

    /**
     * If the underlying transport is UDP, there is no "connect" per say but that method returns
     * directly and as such, no need to have a {@link CompletionStage} etc. Trying to use this
     * method on any other type of transport that doesn't support this "direct" connection method
     * will blow up on an {@link IllegalTransportException}.
     *
     * @param transport
     * @param remoteAddress
     * @return
     * @throws IllegalTransportException in case you try to use this direct connect method with a transport
     *                                   that doesn't support the "direct" connection mode, which is essentially any connection oriented transport.
     *                                   I.e., really only UDP is supporting this.
     */
    Connection<T> connectDirect(Transport transport, InetSocketAddress remoteAddress) throws IllegalTransportException;

    ListeningPoint getListeningPoint(Transport transport);

    /**
     * Check to see if this {@link NetworkInterface} supports the given {@link Transport}.
     */
    boolean isSupportingTransport(Transport transport);
}
