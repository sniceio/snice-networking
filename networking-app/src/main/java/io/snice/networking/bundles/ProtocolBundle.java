package io.snice.networking.bundles;

import com.fasterxml.jackson.databind.Module;
import io.hektor.fsm.Data;
import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.app.impl.DefaultEnvironment;
import io.snice.networking.common.Connection;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.common.fsm.NetworkContext;
import io.snice.networking.netty.ProtocolHandler;

import java.util.List;
import java.util.Optional;

public interface ProtocolBundle<K extends Connection<T>, T, C extends NetworkAppConfig> {

    /**
     * Just a human friendly name of this bundle. Only used for logging.
     */
    String getBundleName();

    Class<T> getType();

    /**
     * During the bootstrapping of the network application, the bundle is asked to
     * create an appropriate {@link Environment}. Many stacks may not care to create
     * a specific environment and as such, may be ok by using the {@link DefaultEnvironment}.
     * If the bundle extends {@link BundleSupport}, by default, the {@link DefaultEnvironment}
     * will be created and returned.
     *
     * @param stack         the underlying {@link NetworkStack}
     * @param configuration the specific configuration of network application.
     * @return
     */
    <E extends Environment<K, T, C>> E createEnvironment(final NetworkStack<K, T, C> stack, C configuration);

    /**
     * An application may need to register it's own
     *
     * @return
     */
    Optional<Module> getObjectMapModule();

    List<ProtocolHandler> getProtocolEncoders();

    List<ProtocolHandler> getProtocolDecoders();

    /**
     * The underlying networking stack does not deal with the potential specialized connection
     * and as such, it is up to the bundle to wrap the "real" {@link Connection} into the
     * application specific connection object.
     *
     * Note: as the name somewhat implies, it is expect that every time the bundle is asked, it will
     * create a new wrapper around it. The bundle, or any stateful system behind the bundle, cannot
     * cache the connection in anyway and as such, must always use the connection passed into to this
     * method every time or it won't work.
     *
     * Note: if the bundle simply does not have a specialized connection class, then simply
     * return the same connection again.
     *
     * @param connection the actual connection that the bundle needs to wrap.
     * @return a new wrapped application specific connection object
     */
    K wrapConnection(Connection<T> connection);

    default void start() {
        // default is to do nothing. Implementing bundles should override this.
    }

    default void stop() {
        // default is to do nothing. Implementing bundles should override this.
    }

    <S extends Enum<S>, C extends NetworkContext<T>, D extends Data> Optional<FsmFactory<T, S, C, D>> getFsmFactory();
}
