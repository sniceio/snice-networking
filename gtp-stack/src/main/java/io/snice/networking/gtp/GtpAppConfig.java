package io.snice.networking.gtp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.snice.networking.app.NetworkAppConfig;

public class GtpAppConfig extends NetworkAppConfig {

    @JsonProperty("gtp")
    private GtpConfig config = new GtpConfig();

    public GtpConfig getConfig() {
        return config;
    }

    public void setConfig(final GtpConfig config) {
        this.config = config;
    }
}
