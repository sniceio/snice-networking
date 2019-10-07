package io.snice.networking.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.snice.networking.config.NetworkInterfaceConfiguration;
import io.snice.networking.config.NetworkInterfaceDeserializer;

import java.io.InputStream;

public class Utils {


    /**
     * Helper method for loading a configuration.
     *
     * @param clazz
     * @param resource
     * @return
     * @throws Exception
     */
    public static <T> T loadConfiguration(final Class<T> clazz, final String resource) throws Exception {
        final InputStream stream = Utils.class.getResourceAsStream(resource);
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(NetworkInterfaceConfiguration.class, new NetworkInterfaceDeserializer());
        mapper.registerModule(module);
        mapper.registerModule(new JavaTimeModule());
        return mapper.readValue(stream, clazz);
    }
}
