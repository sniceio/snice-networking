package io.snice.networking.examples.udp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.snice.networking.config.NetworkInterfaceConfiguration;

import java.util.List;

/**
 * A simple configuration class for the basic {@link UdpServer} example.
 *
 */
public class UdpServerConfig {
    @JsonProperty("network")
    private NetworkInterfaceConfiguration networkConfig;

    public NetworkInterfaceConfiguration getNetworkConfiguration() {
        return networkConfig;
    }
}
