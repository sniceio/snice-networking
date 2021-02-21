package io.snice.networking.app.impl;

import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.common.Connection;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;
import io.snice.networking.core.NetworkInterface;

import java.net.InetSocketAddress;
import java.util.Optional;
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
    public CompletionStage<K> connect(final Transport transport, final InetSocketAddress remoteAddress) throws IllegalTransportException {
        try {
            return (CompletionStage<K>) stack.connect(transport, remoteAddress);
        } catch (final Throwable th) {
            th.printStackTrace();
            throw th;
        }
    }

    @Override
    public CompletionStage<K> connect(final Transport transport, final int localPort, final InetSocketAddress remoteAddress) throws IllegalTransportException {
        return (CompletionStage<K>) stack.connect(transport, localPort, remoteAddress);
    }

    @Override
    public CompletionStage<K> connect(final String name, final Transport transport, final int localPort, final InetSocketAddress remoteAddress) throws IllegalTransportException {
        return (CompletionStage<K>) stack.connect(name, transport, localPort, remoteAddress);
    }

    @Override
    public Optional<NetworkInterface<T>> getNetworkInterface(final String name) {
        return stack.getNetworkInterface(name);
    }


}
