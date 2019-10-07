package io.snice.networking.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.snice.networking.config.NetworkInterfaceConfiguration;

import java.util.Collections;
import java.util.List;

public class NetworkAppConfig {

    @JsonProperty("networkInterfaces")
    private List<NetworkInterfaceConfiguration> networkInterfaces;

    @JsonIgnore
    public List<NetworkInterfaceConfiguration> getNetworkInterfaces() {
        if (networkInterfaces == null) {
            return List.of();
        }

        return networkInterfaces;
    }
}
