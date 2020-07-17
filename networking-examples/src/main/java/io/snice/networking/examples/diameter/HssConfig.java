package io.snice.networking.examples.diameter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.snice.networking.diameter.DiameterAppConfig;
import io.snice.networking.diameter.DiameterConfig;

public class HssConfig extends DiameterAppConfig {

    @JsonProperty("diameter")
    private DiameterConfig diameterConfig;

    public DiameterConfig getDiameterConfig() {
        return diameterConfig;
    }

    public void setDiameterConfig(final DiameterConfig config) {
        this.diameterConfig = config;
    }
}
