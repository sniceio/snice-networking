package io.snice.networking.app;

import io.snice.networking.app.impl.BasicAppBundle;
import io.snice.networking.common.Connection;

public abstract class BasicNetworkApplication<T, C extends NetworkAppConfig> extends NetworkApplication<Connection<T>, T, C> {

    public BasicNetworkApplication(Class<T> type) {
        super(AppBundleRegistry.getDefaultRegistry().find(type)
                .orElseThrow(() -> new IllegalArgumentException("Unable to find an appropriate app bundle for type " + type)));
    }

    public BasicNetworkApplication(AppBundle<Connection<T>, T> bundle) {
        super(bundle);
    }
}
