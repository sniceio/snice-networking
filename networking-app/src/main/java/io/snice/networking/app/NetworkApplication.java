package io.snice.networking.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.snice.buffer.Buffer;
import io.snice.codecs.codec.SerializationFactory;
import io.snice.generics.Generics;
import io.snice.networking.app.impl.DefaultEnvironment;
import io.snice.networking.app.impl.NettyBootstrap;
import io.snice.networking.config.NetworkInterfaceConfiguration;
import io.snice.networking.config.NetworkInterfaceDeserializer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

public abstract class NetworkApplication<T, C extends NetworkAppConfig> {

    private final Class<T> type;
    private Environment<T, C> env;
    private final Optional<AppBundle<T>> bundle;

    public NetworkApplication(final Class<T> type) {
        assertNotNull(type, "The type cannot be null");
        this.type = type;
        this.bundle = Optional.empty();
    }

    public NetworkApplication(final AppBundle<T> bundle) {
        this.bundle = Optional.of(bundle);
        this.type = bundle.getType();
    }

    public void run(final C configuration, final Environment<T, C> environment) {
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
    public abstract void initialize(final NetworkBootstrap<T, C> bootstrap);

    /**
     * Call this method from your {@code public static void main} entry point
     * of your application.
     */
    public final void run(final C config, final String... args) throws Exception {
        final NettyBootstrap<T, C> bootstrap = new NettyBootstrap<>(config);
        initialize(bootstrap);
        final List<ConnectionContext> connectionContexts = bootstrap.getConnectionContexts();

        // TODO: will probably do it differently... better to make use of
        // Netty pipelines, which locks the various things down a little to Netty
        // but whatever. Probably won't integrating with another framework anyway.
        // Thinking about doing some kind of "CodecBundle" where you configure
        // all of those etc etc. Then we can have a NettyCodecBundle
        // final SerializationFactory<T> serializationFactory = ensureSerializationFactory(bootstrap);

        final var builder = NetworkStack.ofType(type)
                .withSerializationFactory(null)
                .withConfiguration(config)
                .withConnectionContexts(connectionContexts)
                .withApplication(this);
        bundle.ifPresent(builder::withAppBundle);

        final var network = builder.build();

        env = buildEnvironment(network, bootstrap);
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

    private SerializationFactory<T> ensureSerializationFactory(final NettyBootstrap<T, C> bootstrap) {
        final var factory = bootstrap.getSerializationFactory();
        if (factory != null) {
            return factory;
        }

        return (SerializationFactory<T>)findDefaultSerializationFactory();
    }

    private SerializationFactory<?> findDefaultSerializationFactory() {
        if (type == String.class) {
            return () -> b -> Optional.of(b.toString());
        }

        if (type == Buffer.class) {
            return () -> Optional::of;
        }

        throw new IllegalArgumentException("You must specify the framer factory in order to convert the incoming byte" +
                "stream across the network into your network stacks object type of " + type.getSimpleName());
    }

    private Environment<T, C> buildEnvironment(final NetworkStack<T, C> stack, final NettyBootstrap<T, C> bootstrap) {
        return new DefaultEnvironment(stack, bootstrap.getConfiguration());
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


        bundle.flatMap(AppBundle::getObjectMapModule).ifPresent(mapper::registerModule);

        return mapper.readValue(stream, getConfigurationClass());
    }

}
