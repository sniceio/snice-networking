package io.snice.networking.examples.echo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.snice.networking.app.NetworkAppConfig;

public class EchoClientConfig extends NetworkAppConfig {

    @JsonProperty
    private String echoServerAddress;

}
