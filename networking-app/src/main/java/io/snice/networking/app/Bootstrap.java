package io.snice.networking.app;

import io.snice.networking.codec.FramerFactory;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

import java.util.function.Predicate;

/**
 * Every {@link NetworkApplication} will be allowed to initialize the logic for how
 * to deal with incoming and outgoing messages and that is achieved through the
 * bootstrap process, where this {@link Bootstrap} class plays a major role.
 *
 * It provides a builder like pattern for creating matching rules for how to deal
 * with the incoming traffic.
 *
 * @param <T> the data type that we'll actually be receiving from the network.
 * @param <C> the configuration type
 */
public interface Bootstrap<T, C extends NetworkAppConfig> {

    C getConfiguration();

    /**
     * You must register a {@link FramerFactory} so that the actual byte stream from the underlying
     * network can be converted into the type that your {@link NetworkApplication} is supposed
     * to be handling.
     *
     * If you do not register a framerFactory, and a default one cannot be found for your type, an
     * exception will be thrown at start-up and the {@link NetworkApplication} will be shutdown.
     *
     * @param framerFactory
     */
    void registerFramer(FramerFactory<T> framerFactory);

    /**
     * Every new incoming connection will be evaluated and configured for, if accepted,
     * future data across that {@link Connection}.
     *
     * Based on the condition, you can create different decision trees on what to do
     * with the incoming connection, such as drop it, drop and send some data back to
     * the remote end etc etc.
     *
     * @param condition
     * @return
     */
    ConnectionContext.Builder<Connection, T, T> onConnection(Predicate<ConnectionId> condition);

}
