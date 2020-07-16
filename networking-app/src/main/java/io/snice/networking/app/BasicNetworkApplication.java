package io.snice.networking.app;

import io.snice.networking.bundles.ProtocolBundleRegistry;
import io.snice.networking.common.Connection;

public abstract class BasicNetworkApplication<T, C extends NetworkAppConfig> extends NetworkApplication<Connection<T>, T, C> {

    public BasicNetworkApplication(final Class<T> type) {
        super(ProtocolBundleRegistry.getDefaultRegistry().find(type)
                .orElseThrow(() -> new IllegalArgumentException("Unable to find an appropriate app bundle for type " + type)));
    }
}
