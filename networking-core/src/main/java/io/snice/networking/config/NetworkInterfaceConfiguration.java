/**
 * 
 */
package io.snice.networking.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.snice.networking.common.Transport;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * @author jonas@jonasborjesson.com
 *
 */
public class NetworkInterfaceConfiguration {

    @JsonProperty
    private final String name;

    /**
     * The IP-address that we will bind to.
     */
    @JsonProperty
    private final URI listen;

    @JsonProperty
    private final URI vipAddress;

    private final List<Transport> transports;

    /**
     * 
     */
    public NetworkInterfaceConfiguration(final String name, final URI listen, final URI vipAddress, final List<Transport> transports) {
        this.name = name;
        this.listen = listen;
        this.vipAddress = vipAddress;
        this.transports = transports;
    }

    public NetworkInterfaceConfiguration(final String name, final URI listen, final URI vipAddress, final Transport ... transports) {
        this(name, listen, vipAddress, Arrays.asList(transports));
    }

    @JsonIgnore
    public boolean hasUDP() {
        return this.transports.contains(Transport.udp);
    }

    @JsonIgnore
    public boolean hasTCP() {
        return this.transports.contains(Transport.tcp);
    }

    @JsonIgnore
    public boolean hasTLS() {
        return this.transports.contains(Transport.tls);
    }

    @JsonIgnore
    public boolean hasWS() {
        return this.transports.contains(Transport.ws);
    }

    @JsonIgnore
    public boolean hasSCTP() {
        return this.transports.contains(Transport.sctp);
    }

    public List<Transport> getTransports() {
        return this.transports;
    }

    public String getName() {
        return this.name;
    }

    @JsonIgnore
    public URI getListeningAddress() {
        return this.listen;
    }

    public URI getVipAddress() {
        return this.vipAddress;
    }

}
