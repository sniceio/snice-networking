package io.snice.networking.app.impl;

import com.fasterxml.jackson.databind.Module;
import io.hektor.fsm.Data;
import io.snice.generics.Generics;
import io.snice.networking.app.AppBundle;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.common.Connection;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.common.fsm.NetworkContext;
import io.snice.networking.netty.ProtocolHandler;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class BasicAppBundle<K extends Connection<T>, T> implements AppBundle<K, T> {


    public Class<T> getType() {
        // System.out.println(Generics.getTypeParameter(this.getClass(), AppBundle.class));
        getType(null);
        return null;
    }

    private Class<K> getType(K connectionType) {
        Type[] types = this.getClass().getGenericInterfaces();
        // Class<T> c = Generics.getTypeParameter(getClass(), Object.class);
        // protected Class<C> getConfigurationClass() {
            // return Generics.getTypeParameter(getClass(), NetworkAppConfig.class);
        // }
        // System.out.println(c);
        // System.out.println(types);

        return null;
    }

    public Class<K> getConnectionType() {
        return null;
    }

    @Override
    public Optional<Module> getObjectMapModule() {
        return Optional.empty();
    }

    @Override
    public List<ProtocolHandler> getProtocolEncoders() {
        return null;
    }

    @Override
    public List<ProtocolHandler> getProtocolDecoders() {
        return null;
    }

    @Override
    public K wrapConnection(Connection<T> connection) {
        return null;
    }

    @Override
    public <S extends Enum<S>, C extends NetworkContext<T>, D extends Data> Optional<FsmFactory<T, S, C, D>> getFsmFactory() {
        return Optional.empty();
    }
}
