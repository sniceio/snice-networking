package io.snice.networking.app;

import com.fasterxml.jackson.databind.Module;
import io.hektor.fsm.Data;
import io.snice.networking.common.Connection;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.common.fsm.NetworkContext;
import io.snice.networking.netty.ProtocolHandler;

import java.util.List;
import java.util.Optional;

public interface AppBundle<K extends Connection<T>, T> {

    Class<T> getType();

    Class<K> getConnectionType();

    /**
     * An application may need to register it's own
     * @return
     */
    Optional<Module> getObjectMapModule();

    List<ProtocolHandler> getProtocolEncoders();

    List<ProtocolHandler> getProtocolDecoders();

    default void start() {
        // default is to do nothing. Implementing bundles should override this.
    }

    <S extends Enum<S>, C extends NetworkContext<T>, D extends Data> Optional<FsmFactory<T, S, C, D>> getFsmFactory();
}
