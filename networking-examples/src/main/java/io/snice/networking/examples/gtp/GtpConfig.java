package io.snice.networking.examples.gtp;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.hektor.config.HektorConfiguration;
import io.snice.networking.gtp.conf.GtpAppConfig;

public class GtpConfig extends GtpAppConfig {

    @JsonProperty("hektor")
    private HektorConfiguration hektorConfig;


    public HektorConfiguration getHektorConfig() {
        return hektorConfig;
    }

    public void setHektorConfig(HektorConfiguration hektorConfig) {
        this.hektorConfig = hektorConfig;
    }

}
