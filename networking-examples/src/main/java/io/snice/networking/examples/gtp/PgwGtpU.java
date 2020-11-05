package io.snice.networking.examples.gtp;

import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.common.Connection;
import io.snice.networking.gtp.GtpApplication;
import io.snice.networking.gtp.event.GtpEvent;

public class PgwGtpU extends GtpApplication<GtpConfig> {

    @Override
    public void initialize(final NetworkBootstrap<Connection<GtpEvent>, GtpEvent, GtpConfig> bootstrap) {
        bootstrap.onConnection(c -> true).accept(b -> {
            b.match(evt -> true).consume((c, gtp) -> {
                System.err.println("Received something else: " + gtp.toMessageEvent().getMessage());
            });
        });
    }

    public static void main(final String... args) throws Exception {
        final var pgw = new PgwGtpU();
        pgw.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/pgw_gtpc.yml");
    }
}
