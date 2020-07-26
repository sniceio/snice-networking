package io.snice.networking.diameter.peer;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.common.Transport;
import io.snice.networking.core.NetworkInterface;
import io.snice.networking.diameter.PeerConnection;
import io.snice.networking.diameter.dns.Resolver;
import io.snice.networking.diameter.dns.SimpleResolver;

import java.net.URI;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * The <code>*Configuration /code> classes, such as {@link PeerConfiguration}, are part of the public API
 * whereas the <code>*Settings</code> are for internal implementation use only. The main reason is simply
 * there are settings for e.g. a {@link PeerConnection} that shouldn't be exposed to the user and anything in the
 * {@link PeerConfiguration} is and also may then be available for configuration in the config files.
 */
public class PeerSettings {

    private final PeerConfiguration config;
    private final NetworkInterface<DiameterMessage> nic;
    private final Transport transport;
    private final Resolver resolver;

    public static Builder of(final PeerConfiguration config) {
        assertNotNull(config);
        return new Builder(config);
    }

    private PeerSettings(final PeerConfiguration config,
                         final NetworkInterface<DiameterMessage> nic,
                         final Transport transport,
                         final Resolver resolver) {
        this.config = config;
        this.nic = nic;
        this.transport = transport;
        this.resolver = resolver;
    }

    public String getName() {
        return config.getName();
    }

    public Peer.MODE getMode() {
        return config.getMode();
    }

    public Transport getTransport() {
        return transport;
    }

    public Resolver getResolver() {
        return resolver;
    }

    public URI getUri() {
        return config.getUri();
    }

    public NetworkInterface<DiameterMessage> getNic() {
        return nic;
    }

    public static class Builder {

        private final PeerConfiguration config;
        private NetworkInterface<DiameterMessage> nic;
        private Transport transport;

        private Builder(final PeerConfiguration config) {
            this.config = config;
        }

        public Builder withNetworkInterface(final NetworkInterface<DiameterMessage> nic) {
            assertNotNull(nic);
            this.nic = nic;
            return this;
        }

        public Builder withTransport(final Transport transport) {
            assertNotNull(transport);
            this.transport = transport;
            return this;
        }

        public PeerSettings build() {
            assertNotNull(nic, "You must specify the Network Interface the peer is supposed to use");
            assertNotNull(transport, "You must specify the Transport the Peer is supposed to use");
            final var resolver = new SimpleResolver();
            return new PeerSettings(config, nic, transport, resolver);
        }
    }
}
