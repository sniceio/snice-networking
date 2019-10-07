package io.snice.networking.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.snice.generics.Generics;
import io.snice.networking.app.impl.DefaultEnvironment;
import io.snice.networking.app.impl.NettyBootstrap;
import io.snice.networking.config.NetworkInterfaceConfiguration;
import io.snice.networking.config.NetworkInterfaceDeserializer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public abstract class NetworkApplication<T extends NetworkAppConfig> {

    private Environment<T> env;

    public abstract void run(T configuration, Environment environment);

    /**
     * As an application, you must override this method and setup your initial routing
     * of incoming messages. For those that are familiar with SIP Servlets (JSR116/289)
     * this would somewhat correspond with the DAR configuration.
     */
    public abstract void initialize(final Bootstrap<T> bootstrap);

    /**
     * Call this method from your {@code public static void main} entry point
     * into your application.
     */
    public final void run(final String... args) throws Exception {
        final T config = loadConfiguration(args[1]);
        final NettyBootstrap<T> bootstrap = new NettyBootstrap<>(config);
        initialize(bootstrap);
        final List<ConnectionContext> connectionContexts = bootstrap.getConnectionContexts();
        env = buildEnvironment(bootstrap);

        final var network = NetworkStack.withConfig(config)
                .withConnectionContexts(connectionContexts)
                .withApplication(this)
                .build();
        network.start();

        // call application
        run(config, env);
        network.sync();

    }

    private Environment buildEnvironment(final NettyBootstrap<T> bootstrap) {
        return new DefaultEnvironment(bootstrap.getConfiguration());
    }

    protected Class<T> getConfigurationClass() {
        return Generics.getTypeParameter(getClass(), NetworkAppConfig.class);
    }

    public T loadConfiguration(final String file) throws Exception {
        final InputStream stream = new FileInputStream(file);
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        // must register the deserializer for the network interface configuration.
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(NetworkInterfaceConfiguration.class, new NetworkInterfaceDeserializer());
        mapper.registerModule(module);

        return mapper.readValue(stream, getConfigurationClass());
    }

}
