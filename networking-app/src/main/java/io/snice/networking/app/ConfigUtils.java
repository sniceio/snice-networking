package io.snice.networking.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.snice.generics.Generics;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.config.NetworkInterfaceConfiguration;
import io.snice.networking.config.NetworkInterfaceDeserializer;

import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigUtils {


    public static <C extends NetworkAppConfig> Class<C> getConfigurationClass(final Class<?> cls) {
        return Generics.getTypeParameter(cls, NetworkAppConfig.class);
    }

    public static <C extends NetworkAppConfig> C loadConfiguration(final Class<C> cls,
                                                                   final ProtocolBundle<?, ?, C> bundle,
                                                                   final String file) throws Exception {
        final byte[] content = Files.readAllBytes(Path.of(file));
        return loadConfiguration(cls, bundle, content);
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
