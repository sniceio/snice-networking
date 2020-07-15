package io.snice.networking.app.impl;

import io.snice.networking.app.AppBundle;
import io.snice.networking.app.AppBundleRegistry;
import io.snice.networking.app.bundles.StringBundle;
import io.snice.networking.common.Connection;
import io.snice.preconditions.PreConditions;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.snice.preconditions.PreConditions.*;

public class DefaultAppBundleRegistry implements AppBundleRegistry {

    private final Map<Class, AppBundle<? extends Connection<?>, ?>> bundles = new HashMap<>();

    // not sure this is doable actually.
    private final Map<String, AppBundle<? extends Connection<?>, ?>> schemeBundles = new HashMap<>();

    private static final AppBundleRegistry DEFAULT_REGISTRY = new DefaultAppBundleRegistry();

    static {
        DEFAULT_REGISTRY.registerBundle(new StringBundle(), String.class, "str");
    }

    public static AppBundleRegistry getDefaultRegistry() {
        return DEFAULT_REGISTRY;
    }

    public <K extends Connection<T>, T> void registerBundle(final AppBundle<K, T> bundle, final Class klass) {
        registerBundle(bundle, klass, null);
    }

    public <K extends Connection<T>, T> void registerBundle(final AppBundle<K, T> bundle, final Class klass, final String scheme) {
        assertNotNull(bundle, "The bundle cannot be null");
        assertNotNull(klass, "The Class cannot be null");
        bundles.put(klass, bundle);
        if (checkIfNotEmpty(scheme)) {
            schemeBundles.put(scheme.toLowerCase(), bundle);
        }
    }

    @Override
    public <K extends Connection<T>, T> Optional<AppBundle<K, T>> find(final Class<T> type) {
        assertNotNull(type, "The type cannot be null");
        final AppBundle<K, T> bundle = (AppBundle<K, T>) bundles.get(type);
        return Optional.ofNullable(bundle);
    }

    /*
    @Override
    public <K extends Connection<T>, T> Optional<AppBundle<K, T>> find(final URI uri) throws IllegalArgumentException {
        final String scheme = uri == null ? null : uri.getScheme();
        assertNotEmpty(scheme, "The URI Scheme, or the URI itself, is null or the empty String");
        final AppBundle<K, T> bundle = (AppBundle<K, T>) schemeBundles.get(scheme);
        return Optional.ofNullable(bundle);
    }
     */
}
