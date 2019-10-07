/**
 * 
 */
package io.snice.networking.core;

import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Simple wrapper around the actual listening address and the optional
 * vip address.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface ListeningPoint {

    int getLocalPort();

    InetSocketAddress getLocalAddress();

    String getLocalIp();

    Transport getTransport();

    URI getListenAddress();

    Optional<URI> getVipAddress();

    /**
     * Bring this {@link ListeningPoint} up, as in have it start
     * listening on its desired ip and port.
     *
     * @return a future, which when successfully completed
     * indicates that we manage to listen to the given address
     */
    CompletableFuture<Void> up();

    /**
     * Bring this {@link ListeningPoint} down, as in have it stop
     * listening on its desired ip and port.
     *
     * @return a future, which when successfully completed
     * indicates that we manage to shut down the port that we
     * previously were listening on.
     */
    CompletableFuture<Void> down();

    /**
     * Connect to the remote address.
     *
     * @param remoteAddress
     * @return
     */
    CompletableFuture<Connection> connect(final InetSocketAddress remoteAddress);

    // because you can't override toString
    default String toStringRepresentation() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getListenAddress().toString());

        if (getVipAddress().isPresent()) {
            sb.append(" as ").append(getVipAddress().toString());
        }
        return sb.toString();
    }

}
