package io.snice.networking.gtp.impl;

import io.snice.networking.app.NetworkStack;
import io.snice.networking.common.Connection;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;
import io.snice.networking.gtp.GtpAppConfig;
import io.snice.networking.gtp.GtpEnvironment;
import io.snice.networking.gtp.event.GtpEvent;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

public class DefaultGtpEnvironment<C extends GtpAppConfig> implements GtpEnvironment<C> {

    private final NetworkStack<Connection<GtpEvent>, GtpEvent, C> stack;
    private final C config;

    public DefaultGtpEnvironment(final NetworkStack<Connection<GtpEvent>, GtpEvent, C> stack, final C config) {
        this.stack = stack;
        this.config = config;
    }

    @Override
    public C getConfig() {
        return config;
    }

    @Override
    public CompletionStage<Connection<GtpEvent>> connect(final Transport transport, final InetSocketAddress remoteAddress) throws IllegalTransportException {
        throw new RuntimeException("Not implemented just yet");
    }
}
