package io.snice.networking.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.snice.generics.Generics;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.config.NetworkInterfaceConfiguration;
import io.snice.networking.config.NetworkInterfaceDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ConfigUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    public static <C extends NetworkAppConfig> Class<C> getConfigurationClass(final Class<?> cls) {
        return Generics.getTypeParameter(cls, NetworkAppConfig.class);
    }

    public static <C extends NetworkAppConfig> C loadConfiguration(final Class<C> cls,
                                                                   final ProtocolBundle<?, ?, C> bundle,
                                                                   final String file) throws Exception {
        final var path = findConfigFile(file);
        final byte[] content = Files.readAllBytes(path);
        return loadConfiguration(cls, bundle, content);
    }

    /**
     * We'll try to do everything we can to find the given config file, including searching any loaded
     * jars for it. The process is as follows:
     * <ol>
     *     <li>Just see if the file exists, which would mean the user has probably given us
     *     a file that exists on the local file system
     *     </li>
     *     <li>If no 1 fails, then see if it is included in a jar and load it from there.</li>
     * </ol>
     *
     * @param file
     * @return
     */
    private static Path findConfigFile(final String file) throws FileNotFoundException {
        return getFromFileSystem(file)
                .or(() -> getFromJar(file))
                .or(() -> getFromJar("/" + file))
                .orElseThrow(() -> new FileNotFoundException("Unable to find the file " + file));
    }

    private static Optional<Path> getFromJar(final String file) {
        logger.info("Trying to load configuration file \"" + file + "\" as a resource from our loaded jars");
        final var url = ConfigUtils.class.getResource(file);
        if (url == null) {
            return Optional.empty();
        }
        try {
            final var path = Path.of(url.toURI());
            if (path.toFile().exists() && path.toFile().canRead()) {
                return Optional.of(path);
            }
        } catch (final URISyntaxException e) {
            // ignore
        }

        return Optional.empty();
    }

    private static Optional<Path> getFromFileSystem(final String file) {
        logger.info("Trying to load configuration file \"" + file + "\" from the file system");
        final var path = Path.of(file);
        if (path.toFile().exists() && path.toFile().canRead()) {
            return Optional.of(path);
        }
        return Optional.empty();
    }

    public static <C extends NetworkAppConfig> C loadConfiguration(final Class<C> cls,
                                                                   final byte[] content) throws Exception {
        return loadConfiguration(cls, null, content);
    }

    public static <C extends NetworkAppConfig> C loadConfiguration(final Class<C> cls,
                                                                   final ProtocolBundle<?, ?, C> bundle,
                                                                   final byte[] content) throws Exception {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        // must register the deserializer for the network interface configuration.
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(NetworkInterfaceConfiguration.class, new NetworkInterfaceDeserializer());
        mapper.registerModule(module);
        mapper.registerModule(new Jdk8Module());

        if (bundle != null) {
            bundle.getObjectMapModule().ifPresent(mapper::registerModule);
        }
        return mapper.readValue(content, cls);
    }

}
