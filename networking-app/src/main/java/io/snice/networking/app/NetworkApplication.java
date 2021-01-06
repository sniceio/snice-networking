package io.snice.networking.app;

import io.snice.networking.app.impl.GenericBootstrap;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;

import java.util.List;

import static io.snice.networking.app.ConfigUtils.getConfigurationClass;
import static io.snice.networking.app.ConfigUtils.loadConfiguration;
import static io.snice.preconditions.PreConditions.assertNotNull;
import static io.snice.preconditions.PreConditions.ensureNotNull;

public abstract class NetworkApplication<E extends Environment<K, T, C>, K extends Connection<T>, T, C extends NetworkAppConfig> {

    private E env;
    private final ProtocolBundle<K, T, C> bundle;
    private NetworkStack<K, T, C> network;

    public NetworkApplication(final ProtocolBundle<K, T, C> bundle) {
        assertNotNull(bundle, "The app bundle cannot be null");
        this.bundle = bundle;
    }

    public void run(final C configuration, final E environment) {
        // sub-classes may override this method in order to setup additional
        // resources etc as the application starts running.
    }

    /**
     * As an application, you must override this method and setup your initial routing
     * of incoming messages. For those that are familiar with SIP Servlets (JSR116/289)
     * this would somewhat correspond with the DAR configuration.
     *
     * TODO: this is really only needed for the server when you listen on incoming connection.
     * If you are only acting as a client, you shouldn't need this but rather setup the rules
     * on the connection and it's chain when you establish that connection.
     *
     * So, make this one optional and then in the {@link Environment} you rather return a similar
     * builder object as the bootstrap has etc etc.
     */
    public abstract void initialize(final NetworkBootstrap<K, T, C> bootstrap);

    protected ProtocolBundle<K, T, C> getProtocolBundle() {
        return bundle;
    }

    /**
     * Call this method from your {@code public static void main} entry point
     * of your application.
     */
    public final void run(final C config, final String... args) throws Exception {
        final GenericBootstrap<K, T, C> bootstrap = new GenericBootstrap<>(config);
        initialize(bootstrap);
        final List<ConnectionContext<K, T>> connectionContexts = bootstrap.getConnectionContexts();

        bundle.initialize(config);

        final NetworkStack.Builder<E, K, T, C> builder = NetworkStack.withConfiguration(config);
        builder.withConnectionContexts(connectionContexts);
        builder.withApplication(this);
        builder.withAppBundle(bundle);

        network = builder.build();
        env = bundle.createEnvironment(network, bootstrap.getConfiguration());
        ensureNotNull(env, "Bundle \"" + bundle.getBundleName()
                + "\" produced a null value for the Environment. Stack is shutting down");
        network.start();
        bundle.start(network);

        // call application
        run(config, env); // TODO: app can throw exception. Handle it.
    }

    /**
     * Call this method from your {@code public static void main} entry point
     * into your application.
     */
    public final void run(final String... args) throws Exception {
        // TODO: actually call a bootstrap method here that allows the app
        // to register the AppBundle (rename it to NetworkStackBundle or something)

        // killing the first arg but to be backwards compatible, let's see
        // if there are 1 or two args.
        final String confFile;
        if (args.length == 1) {
            confFile = args[0];
        } else if (args.length == 2 && "server".equalsIgnoreCase(args[0])) {
            confFile = args[1];
        } else {
            throw new IllegalArgumentException("Unable to determine which one of the arguments is the configuration file");
        }

        final Class<C> cls = getConfigurationClass(getClass());
        final C config = loadConfiguration(cls, bundle, confFile);
        run(config, args);
    }

    public final void stop() {
        network.stop();
        // return network.sync();
    }

}
