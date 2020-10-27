package io.snice.networking.examples.gtp;

import io.netty.util.NetUtil;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.common.Connection;
import io.snice.networking.examples.diameter.HssApp;
import io.snice.networking.gtp.GtpApplication;
import io.snice.networking.gtp.event.GtpEvent;

public class PgwApp extends GtpApplication<GtpConfig> {

    @Override
    public void initialize(final NetworkBootstrap<Connection<GtpEvent>, GtpEvent, GtpConfig> bootstrap) {
        bootstrap.onConnection(c -> true).accept(b -> {
            b.match(evt -> true).consume((c, gtp) -> c.close());
        });
    }

    public static void main(final String... args) throws Exception {
        final var pgw = new PgwApp();
        pgw.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/PGW.yml");
    }
}
