package io.snice.networking.diameter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.snice.networking.app.NetworkAppConfig;

public class DiameterAppConfig extends NetworkAppConfig {

    @JsonProperty("diameter")
    private DiameterConfig config = new DiameterConfig();

    public DiameterConfig getConfig() {
        return config;
    }

    public void setConfig(DiameterConfig config) {
        this.config = config;
    }
}
