package io.snice.networking.examples.gtp;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.snice.networking.gtp.conf.GtpAppConfig;

public class GtpProxyConfig extends GtpAppConfig {

    @JsonProperty("proxyToAddress")
    private String proxyToAddress;

    @JsonProperty("proxyToPort")
    private int proxyToPort;

    public String getProxyToAddress() {
        return proxyToAddress;
    }

    public void setProxyToAddress(final String proxyToAddress) {
        this.proxyToAddress = proxyToAddress;
    }

    public int getProxyToPort() {
        return proxyToPort;
    }

    public void setProxyToPort(final int proxyToPort) {
        this.proxyToPort = proxyToPort;
    }
}
