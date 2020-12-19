package io.snice.networking.app;

import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.bundles.ProtocolBundleRegistry;
import io.snice.networking.common.Connection;

public abstract class BasicNetworkApplication<T, C extends NetworkAppConfig> extends NetworkApplication<Environment<Connection<T>, T, C>, Connection<T>, T, C> {

    public BasicNetworkApplication(final Class<T> type) {
        super((ProtocolBundle<Connection<T>, T, C>) ProtocolBundleRegistry.getDefaultRegistry().find(type)
                .orElseThrow(() -> new IllegalArgumentException("Unable to find an appropriate app bundle for type " + type)));
    }

    public BasicNetworkApplication(final ProtocolBundle<Connection<T>, T, C> bundle) {
        super(bundle);
    }
}
