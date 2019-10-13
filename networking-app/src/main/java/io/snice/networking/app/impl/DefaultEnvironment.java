package io.snice.networking.app.impl;

import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkAppConfig;

public class DefaultEnvironment <T, C extends NetworkAppConfig> implements Environment<T, C> {
    private final C config;

    public DefaultEnvironment(final C config) {
        this.config = config;
    }

    @Override
    public C getConfig() {
        return config;
    }
}
