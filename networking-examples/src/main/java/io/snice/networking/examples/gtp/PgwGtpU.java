package io.snice.networking.examples.gtp;

import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;
import io.snice.networking.gtp.GtpApplication;
import io.snice.networking.gtp.GtpEnvironment;
import io.snice.networking.gtp.GtpUserTunnel;
import io.snice.networking.gtp.PdnSession;
import io.snice.networking.gtp.event.GtpEvent;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

public class PgwGtpU extends GtpApplication<GtpConfig> {

    private final AtomicReference<GtpEnvironment<GtpConfig>> environment = new AtomicReference<>();

    @Override
    public void initialize(final NetworkBootstrap<Connection<GtpEvent>, GtpEvent, GtpConfig> bootstrap) {
        bootstrap.onConnection(c -> true).accept(b -> {
            b.match(evt -> true).consume((c, gtp) -> {
                System.err.println("Received something else: " + gtp.toMessageEvent().getMessage());
            });
        });
    }

    public CompletionStage<GtpUserTunnel> establishTunnel(final Optional<String> natAddress, final PdnSession session) {
        final var remoteBearerAddress = natAddress.orElse(session.getDefaultRemoteBearer().getIPv4AddressAsString().get());
        final var remotePort = 2152;

        return environment.get().connect(Transport.udp, remoteBearerAddress, remotePort)
                .thenApply(c -> GtpUserTunnel.of(c, session.getPaa(), session.getDefaultLocalBearer(), session.getDefaultRemoteBearer()));
    }

    @Override
    public void run(final GtpConfig configuration, final GtpEnvironment<GtpConfig> environment) {
        this.environment.set(environment);

        environment.connect(Transport.udp, "3.89.210.241", 2154).thenAccept(c -> {
        });
    }
}
