package io.snice.networking.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.snice.generics.Generics;
import io.snice.networking.app.impl.NettyBootstrap;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.config.NetworkInterfaceConfiguration;
import io.snice.networking.config.NetworkInterfaceDeserializer;
import io.snice.preconditions.PreConditions;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import static io.snice.preconditions.PreConditions.ensureNotNull;

public abstract class NetworkApplication<K extends Connection<T>, T, C extends NetworkAppConfig> {

    // private final Class<T> type;
    // private final Class<K> connectionType;
    private Environment<K, T, C> env;
    private final ProtocolBundle<K, T> bundle;

    /**
     * Constructor that assumes that the connection type is
     * just the regular base {@link Connection} object and not
     * a specific sub-class (such as Peer for Diameter)
     *
     */
    /*
    public NetworkApplication(final Class<T> type) {
        // scap this. regular network application needs a bundle and that's is
            // then the basic networking app can figure out the basic bundles
        assertNotNull(type, "The type cannot be null");
        // this.type = type;
        // this.connectionType = null;
        this.bundle = null;
    }

     */

    /*
    public NetworkApplication(final Class<T> type, final Class<K> connectionType) {
        assertNotNull(type, "The type cannot be null");
        assertNotNull(connectionType, "The type cannot be null");
        // this.type = type;
        // this.connectionType = connectionType;
        this.bundle = null;
    }
     */
    public NetworkApplication(final ProtocolBundle<K, T> bundle) {
        PreConditions.assertNotNull(bundle, "The app bundle cannot be null");
        this.bundle = bundle;
    }

    public void run(final C configuration, final Environment<K, T, C> environment) {
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

    /**
     * Call this method from your {@code public static void main} entry point
     * of your application.
     */
    public final void run(final C config, final String... args) throws Exception {
        final NettyBootstrap<K, T, C> bootstrap = new NettyBootstrap<>(config);
        initialize(bootstrap);
        final List<ConnectionContext> connectionContexts = bootstrap.getConnectionContexts();

        final NetworkStack.Builder<K, T, C> builder = NetworkStack.withConfiguration(config);
        builder.withConnectionContexts(connectionContexts);
        builder.withApplication(this);
        builder.withAppBundle(bundle);

        final var network = builder.build();
        env = bundle.createEnvironment(network, bootstrap.getConfiguration());
        ensureNotNull(env, "Bundle \"" + bundle.getBundleName()
                + "\" produced a null value for the Environment. Stack is shutting down");
        network.start();

        // call application
        run(config, env); // TODO: app can throw exception. Handle it.
        network.sync();
    }

    /**
     * Call this method from your {@code public static void main} entry point
     * into your application.
     */
    public final void run(final String... args) throws Exception {
        // TODO: actually call a bootstrap method here that allows the app
        // to register the AppBundle (rename it to NetworkStackBundle or something)

        final C config = loadConfiguration(args[1]);
        run(config, args);
    }

    protected Class<C> getConfigurationClass() {
        return Generics.getTypeParameter(getClass(), NetworkAppConfig.class);
    }

    public C loadConfiguration(final String file) throws Exception {
        final InputStream stream = new FileInputStream(file);
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        // must register the deserializer for the network interface configuration.
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(NetworkInterfaceConfiguration.class, new NetworkInterfaceDeserializer());
        mapper.registerModule(module);

        bundle.getObjectMapModule().ifPresent(mapper::registerModule);
        return mapper.readValue(stream, getConfigurationClass());
    }

}
