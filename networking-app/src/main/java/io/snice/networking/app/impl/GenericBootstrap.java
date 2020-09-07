package io.snice.networking.app.impl;

import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class GenericBootstrap<K extends Connection<T>, T, C extends NetworkAppConfig> implements NetworkBootstrap<K, T, C> {

    private final C config;

    private final List<ConnectionCtxBuilder<K, T, ?>> rules = new ArrayList<>();

    public GenericBootstrap(final C config) {
        this.config = config;
    }

    public List<ConnectionContext<K, T>> getConnectionContexts() {
        return rules.stream().map(ConnectionCtxBuilder::build).collect(Collectors.toList());
    }

    @Override
    public C getConfiguration() {
        return config;
    }

    /*
    @Override
    public void registerSerializationFactory(final SerializationFactory<T> serializationFactory) {
        assertNotNull(serializationFactory, "The serializationFactory cannot be null");
        this.serializationFactory = serializationFactory;
    }
     */

    @Override
    public ConnectionContext.Builder<K, T, T> onConnection(final Predicate<ConnectionId> condition) {
        assertNotNull(condition, "The condition cannot be null");
        final ConnectionCtxBuilder<K, T, T> builder = new ConnectionCtxBuilder<>(condition);
        rules.add(builder);
        return builder;
    }

}
