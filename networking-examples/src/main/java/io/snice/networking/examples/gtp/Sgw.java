package io.snice.networking.examples.gtp;


/**
 * Sample app that ties together both GTP-C and GTP-U
 */
public class Sgw {

    private final PgwGtpU gtpu;
    private final PgwGtpC gtpc;

    public Sgw(final PgwGtpU gtpu, final PgwGtpC gtpc) {
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
        final var pgw = new Sgw(gtpu, gtpc);
        pgw.start();
    }
}
