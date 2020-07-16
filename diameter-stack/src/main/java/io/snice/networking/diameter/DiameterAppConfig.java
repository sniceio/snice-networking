package io.snice.networking.diameter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.snice.networking.app.NetworkAppConfig;

public class DiameterAppConfig extends NetworkAppConfig {

    @JsonProperty("diameter")
    private DiameterConfig config;

    public DiameterConfig getConfig() {
        return config;
    }
}
