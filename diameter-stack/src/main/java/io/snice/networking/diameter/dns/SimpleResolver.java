package io.snice.networking.diameter.dns;

import io.snice.networking.common.Transport;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Simple {@link Resolver} that only does A (or AAAA) record lookups and will NOT
 * make any attempts to resolve the host via S-NAPTR -> SRV -> A/AAAA. Hence, if the
 * port is not given within the {@link URI} then the default port for the
 * given transport is going to be used (transport must then be configured too in the URI)
 */
public class SimpleResolver implements Resolver {

    @Override
    public CompletionStage<InetSocketAddress> resolve(final URI uri) {
        // TODO: just convert the URI into a DiameterUri ala SipUri
        final var host = uri.getHost();
        final var transport = Resolver.getTransport(uri);
        final var port = uri.getPort() == -1 ? Resolver.getDefaultPort(transport) : uri.getPort();
        final var remoteAddress = new InetSocketAddress(host, port);
        return CompletableFuture.completedFuture(remoteAddress);
    }
}
