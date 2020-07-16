package io.snice.networking.bundles;

import io.snice.networking.app.impl.DefaultProtocolBundleRegistry;
import io.snice.networking.common.Connection;

import java.net.URI;
import java.util.Optional;

/**
 *
 */
public interface ProtocolBundleRegistry {

    static ProtocolBundleRegistry getDefaultRegistry() {
        return DefaultProtocolBundleRegistry.getDefaultRegistry();
    }

    <K extends Connection<T>, T> void registerBundle(final ProtocolBundle<K, T> bundle, final Class klass);

    <K extends Connection<T>, T> void registerBundle(final ProtocolBundle<K, T> bundle, final Class klass, final String scheme);

    /**
     * Find an {@link ProtocolBundle} given a specific type.
     *
     * @param type the type we expect the bundle to handle. I.e., this is the type that the
     *             network stack will be dealing with.
     */
    <K extends Connection<T>, T> Optional<ProtocolBundle<K, T>> find(Class<T> type);

    /**
     * Find an {@link ProtocolBundle} based on the URI schema. E.g., "aaa" would attempt to find an
     * {@link ProtocolBundle} for the Diameter protocol. "sip" would do so for the SIP protocol etc.
     *
     * @param uri
     * @throws IllegalArgumentException in case there is no schema in the given {@link URI}
     */
    // <K extends Connection<T>, T> Optional<AppBundle<K, T>> find(URI uri) throws IllegalArgumentException;
}
