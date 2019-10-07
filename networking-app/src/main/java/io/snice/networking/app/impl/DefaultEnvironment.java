package io.snice.networking.app.impl;

import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkAppConfig;

public class DefaultEnvironment <T extends NetworkAppConfig> implements Environment<T> {
    private final T config;

    public DefaultEnvironment(final T config) {
        this.config = config;
    }

    @Override
    public T getConfig() {
        return config;
    }
}
