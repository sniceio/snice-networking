package io.snice.networking.app.impl;

import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.common.Connection;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

public class DefaultEnvironment <K extends Connection<T>, T, C extends NetworkAppConfig> implements Environment<K, T, C> {

    private final NetworkStack<K, T, C> stack;
    private final C config;

    public DefaultEnvironment(final NetworkStack<K, T, C> stack, final C config) {
        this.stack = stack;
        this.config = config;
    }

    @Override
    public C getConfig() {
        return config;
    }

    @Override
    public CompletionStage<Connection<T>> connect(final Transport transport, final InetSocketAddress remoteAddress) throws IllegalTransportException {
        return stack.connect(transport, remoteAddress);
    }
}
