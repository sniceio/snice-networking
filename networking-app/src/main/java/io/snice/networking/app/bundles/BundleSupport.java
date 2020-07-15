package io.snice.networking.app.bundles;

import com.fasterxml.jackson.databind.Module;
import io.hektor.fsm.Data;
import io.snice.networking.app.AppBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.common.fsm.NetworkContext;

import java.util.Optional;

public abstract class BundleSupport<K extends Connection<T>, T> implements AppBundle<K, T> {

    private final Class<T> type;

    public BundleSupport(final Class<T> type) {
        this.type = type;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public Optional<Module> getObjectMapModule() {
        return Optional.empty();
    }

    @Override
    public K wrapConnection(final Connection<T> connection) {
        return (K) connection;
    }

    @Override
    public <S extends Enum<S>, C extends NetworkContext<T>, D extends Data> Optional<FsmFactory<T, S, C, D>> getFsmFactory() {
        return Optional.empty();
    }
}
