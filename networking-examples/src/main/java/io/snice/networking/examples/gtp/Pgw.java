package io.snice.networking.examples.gtp;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.networking.app.BasicNetworkApplication;
import io.snice.networking.app.Environment;
import io.snice.networking.app.MessagePipe;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

import static io.snice.networking.app.MessagePipe.match;


/**
 * Sample app that ties together both GTP-C and GTP-U
 */
public class Pgw {

    private final PgwGtpU gtpu;
    private final PgwGtpC gtpc;

    public Pgw(final PgwGtpU gtpu, final PgwGtpC gtpc) {
        this.gtpu = gtpu;
        this.gtpc = gtpc;
    }

    public void start() throws Exception {
        gtpu.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/pgw_gtpu.yml");
        gtpc.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/pgw_gtpc.yml");
    }

    public static void main(final String... args) throws Exception {
        final var gtpu = new PgwGtpU();
        final var gtpc = new PgwGtpC();
        final var pgw = new Pgw(gtpu, gtpc);
        pgw.start();
    }
}
