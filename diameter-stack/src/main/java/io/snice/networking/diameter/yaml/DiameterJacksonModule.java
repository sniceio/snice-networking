package io.snice.networking.diameter.yaml;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

public class DiameterJacksonModule extends Module {

    // TODO: generate from pom.xml. See how jackson-datatype-jdk8 has set it up
    final Version VERSION = Version.unknownVersion();

    public DiameterJacksonModule() {
        // left empty intentionally
    }

    @Override
    public String getModuleName() {
        return "Diameter";
    }

    @Override
    public Version version() {
        return VERSION;
    }

    @Override
    public void setupModule(final SetupContext context) {
        // context.addDeserializers();
    }
}
