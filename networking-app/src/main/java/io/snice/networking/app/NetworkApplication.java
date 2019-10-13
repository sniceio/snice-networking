package io.snice.networking.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.snice.generics.Generics;
import io.snice.networking.app.impl.DefaultEnvironment;
import io.snice.networking.app.impl.NettyBootstrap;
import io.snice.networking.codec.FramerFactory;
import io.snice.networking.config.NetworkInterfaceConfiguration;
import io.snice.networking.config.NetworkInterfaceDeserializer;
import io.snice.preconditions.PreConditions;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import static io.snice.preconditions.PreConditions.assertNotNull;

public abstract class NetworkApplication<T, C extends NetworkAppConfig> {

    private final Class<T> type;
    private Environment<T, C> env;

    public NetworkApplication(Class<T> type) {
        assertNotNull(type, "The type cannot be null");
        this.type = type;
    }

    public abstract void run(C configuration, Environment<T, C> environment);

    /**
     * As an application, you must override this method and setup your initial routing
     * of incoming messages. For those that are familiar with SIP Servlets (JSR116/289)
     * this would somewhat correspond with the DAR configuration.
     */
    public abstract void initialize(final Bootstrap<T, C> bootstrap);

    /**
     * Call this method from your {@code public static void main} entry point
     * into your application.
     */
    public final void run(final String... args) throws Exception {
        final C config = loadConfiguration(args[1]);
        final NettyBootstrap<T, C> bootstrap = new NettyBootstrap<>(config);
        initialize(bootstrap);
        final List<ConnectionContext> connectionContexts = bootstrap.getConnectionContexts();
        final FramerFactory<T> framerFactory = ensureFramerFactory(bootstrap);
        env = buildEnvironment(bootstrap);

        final var network = NetworkStack.ofType(type)
                .withFramerFactory(framerFactory)
                .withConfiguration(config)
                .withConnectionContexts(connectionContexts)
                .withApplication(this)
                .build();
        network.start();

        // call application
        run(config, env);
        network.sync();

    }

    private FramerFactory<T> ensureFramerFactory(NettyBootstrap<T, C> bootstrap) {
        final FramerFactory<T> framerFactory = bootstrap.getFramerFactory();
        assertNotNull(framerFactory, "You must specify the framer factory in order to convert the incoming byte" +
                "stream across the network into your network stacks object type of " + type.getSimpleName());
        return framerFactory;
    }

    private Environment<T, C> buildEnvironment(final NettyBootstrap<T, C> bootstrap) {
        return new DefaultEnvironment(bootstrap.getConfiguration());
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

        return mapper.readValue(stream, getConfigurationClass());
    }

}
