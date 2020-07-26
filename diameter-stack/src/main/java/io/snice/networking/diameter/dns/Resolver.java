package io.snice.networking.diameter.dns;

import io.snice.networking.common.Transport;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CompletionStage;

public interface Resolver {

    /**
     * Resolve the given host & port into a {@link InetSocketAddress}. If the host
     * is already an IP address this method will return immediately with a
     * successful {@link CompletionStage}. If the host is a FQDN, it will try
     * to be resolved via an A, or AAAA, record.
     */
    CompletionStage<InetSocketAddress> resolve(URI uri);

    static int getDefaultPort(final Transport transport) {
        switch (transport) {
            case tcp:
                return 3869;
            default:
                return 3868;
        }
    }

    static Transport getTransport(final URI uri) {
        return Transport.tcp;
    }

}
