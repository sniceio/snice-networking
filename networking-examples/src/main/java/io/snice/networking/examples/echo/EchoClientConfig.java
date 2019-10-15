package io.snice.networking.examples.echo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.snice.networking.app.NetworkAppConfig;

public class EchoClientConfig extends NetworkAppConfig {

    @JsonProperty("remoteHost")
    private String echoServerIp;

    @JsonProperty("remoteIp")
    private int echoServerPort;

    public String getEchoServerIp() {
        return echoServerIp;
    }

    public int getEchoServerPort() {
        return echoServerPort;
    }
}
