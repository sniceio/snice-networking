package io.snice.networking.bundles;

import com.fasterxml.jackson.databind.Module;
import io.hektor.fsm.Data;
import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.app.impl.DefaultEnvironment;
import io.snice.networking.common.Connection;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.common.fsm.NetworkContext;
import io.snice.networking.netty.ProtocolHandler;

import javax.annotation.processing.Completions;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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

    /**
     * The bundle will be asked to perform any initializing tasks after the {@link NetworkBootstrap} has
     * been bootstrapped but before the underlying {@link NetworkStack} has been built and started.
     * As such, the only thing available to the bundle at this stage is the actual configuration object.
     *
     * If curious, see {@link io.snice.networking.app.NetworkApplication#run(NetworkAppConfig, String...)} for
     * how it's done.
     *
     * @param configuration
     */
    default void initialize(C configuration) {
        // default is to do nothing. Implementing bundles should override this.
    }

    /**
     * The bundle will be asked to start after it has been initialized and after
     * the underlying {@link NetworkStack} has been started and all its interfaces
     * have been created and bound to their configured listening points. Hence, when
     * the bundle is asked to start, the full underlying network stack is up and running
     * and is, and is allowed, to be used to its fullest. Also feel free to save the reference
     * to the {@link NetworkStack}, it is threadsafe.
     *
     * If the {@link NetworkStack} is unable to start for any reason, the bundle will not
     * be asked to start since we will bail out earlier (try to configure and bind to a non-existing local IP
     * and you'll see).
     *
     * @param stack the initialized, started and fully operational {@link NetworkStack}.
     */
    default CompletionStage<ProtocolBundle<K, T, C>> start(final NetworkStack<K, T, C> stack) {
        // default is to do nothing. Implementing bundles should override this.
        return CompletableFuture.completedFuture(this);
    }

    /**
     * The bundle will be asked to stop, typically, when the entire network stack is going down. However,
     * there is nothing to prevent some random application logic to ask the bundle to stop but the thing to know
     * is that if it is part of shutting down the entire stack, the actual {@link NetworkStack} will be asked to
     * stop AFTER the bundles have been asked to stop. As such, the actual {@link NetworkStack} is still fully
     * functioning but it is highly recommended to not ask the network stack to initiate new connections etc as part
     * of the bundle being shut down. You can (perhaps you want to ping something as you go down?) but be careful
     * since it will delay the stopping of the stack.
     */
    default void stop() {
        // default is to do nothing. Implementing bundles should override this.
    }

    <S extends Enum<S>, C extends NetworkContext<T>, D extends Data> Optional<FsmFactory<T, S, C, D>> getFsmFactory();
}
