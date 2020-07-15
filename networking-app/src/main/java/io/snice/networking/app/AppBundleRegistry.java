package io.snice.networking.app;

import io.snice.networking.app.impl.DefaultAppBundleRegistry;
import io.snice.networking.common.Connection;

import java.net.URI;
import java.util.Optional;

/**
 *
 */
public interface AppBundleRegistry {

    static AppBundleRegistry getDefaultRegistry() {
        return DefaultAppBundleRegistry.getDefaultRegistry();
    }

    <K extends Connection<T>, T> void registerBundle(final AppBundle<K, T> bundle, final Class klass);

    <K extends Connection<T>, T> void registerBundle(final AppBundle<K, T> bundle, final Class klass, final String scheme);

    /**
     * Find an {@link AppBundle} given a specific type.
     *
     * @param type the type we expect the bundle to handle. I.e., this is the type that the
     *             network stack will be dealing with.
     */
    <K extends Connection<T>, T> Optional<AppBundle<K, T>> find(Class<T> type);

    /**
     * Find an {@link AppBundle} based on the URI schema. E.g., "aaa" would attempt to find an
     * {@link AppBundle} for the Diameter protocol. "sip" would do so for the SIP protocol etc.
     *
     * @param uri
     * @throws IllegalArgumentException in case there is no schema in the given {@link URI}
     */
    // <K extends Connection<T>, T> Optional<AppBundle<K, T>> find(URI uri) throws IllegalArgumentException;
}
