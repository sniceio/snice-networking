package io.snice.networking.core;

import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author jonas@jonasborjesson.com
 */
public interface NetworkLayer {

    /**
     * Start the {@link NetworkLayer}, which will call {@link NetworkInterface#up()} on all
     * the configured network interfaces. The method will hang until all network interfaces
     * have completed, either successfully or in error.
     */
    void start();

    /**
     * Stop the network stack. This is a blocking method and will as such not return
     * until all of the network interfaces have been shutdown.
     */
    void stop();

    /**
     * Attempt to connect to the specified address using the specified transport protocol.
     * The default {@link NetworkInterface} will be used.
     *
     * This is a convenience method for
     *
     * @param address
     * @param transport
     * @return
     */
    CompletableFuture<Connection> connect(Transport transport, InetSocketAddress address);

    /**
     * Return a {@link CompletionStage} that will complete once the {@link NetworkLayer} has
     * been shutdown.
     *
     */
    CompletionStage<Void> sync();

    /**
     * A {@link NetworkLayer} will always have a default {@link NetworkInterface}, which
     * has either been explicitly configured or else it will simply be the first one
     * configured with this {@link NetworkLayer}.
     *
     * @return
     */
    NetworkInterface getDefaultNetworkInterface();

    /**
     * Get the named {@link NetworkInterface}.
     *
     * @param name the name of the interface
     * @return an optional with the named interface if found,
     * otherwise an empty optional will be returned.
     */
    Optional<? extends NetworkInterface> getNetworkInterface(String name);

    /**
     * Get a list of all the {@link NetworkInterface}s that has been configured.
     *
     * @return
     */
    List<? extends NetworkInterface> getNetworkInterfaces();

    /**
     * Same as {@link NetworkLayer#getListeningPoint(String, Transport)} but we will
     * grab the default {@link NetworkInterface}.
     *
     * @param transport
     * @return
     */
    Optional<ListeningPoint> getListeningPoint(Transport transport);

    /**
     * Try and get a listening point that can be used to send messages across using the
     * specified {@link Transport} over the named {@link NetworkInterface}.
     *
     * @param networkInterfaceName
     * @param transport
     * @return
     */
    Optional<ListeningPoint> getListeningPoint(String networkInterfaceName, Transport transport);
}
