package io.snice.networking.examples.gtp;


import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.networking.gtp.PdnSession;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Sample app that ties together both GTP-C and GTP-U
 */
public class Sgw implements TunnelManagement {

    private final PgwGtpU gtpu;
    private final PgwGtpC gtpc;

    private final ConcurrentMap<Teid, PdnSession> pdnSessions = new ConcurrentHashMap<>();

    /**
     * dns query for google.com. Grabbed from wireshark
     */
    final static Buffer dnsQuery = Buffer.of(
            (byte) 0x5c, (byte) 0x79, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x77, (byte) 0x77, (byte) 0x77,
            (byte) 0x06, (byte) 0x67, (byte) 0x6f, (byte) 0x6f, (byte) 0x67, (byte) 0x6c, (byte) 0x65, (byte) 0x03,
            (byte) 0x63, (byte) 0x6f, (byte) 0x6d, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01);


    public Sgw(final PgwGtpU gtpu, final PgwGtpC gtpc) {
        this.gtpu = gtpu;
        this.gtpc = gtpc;
    }

    public void start() throws Exception {
        gtpc.setTunnelManagement(this);
        gtpu.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/pgw_gtpu.yml");
        gtpc.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/pgw_gtpc.yml");
    }

    public static void main(final String... args) throws Exception {
        final var gtpu = new PgwGtpU();
        final var gtpc = new PgwGtpC();
        final var sgw = new Sgw(gtpu, gtpc);
        sgw.start();
    }

    @Override
    public void onPdnSessionAccepted(final PdnSession session) {
        System.err.println("Accepting the Pdn session");
        final var previous = pdnSessions.putIfAbsent(session.getLocalTeid(), session);
        if (previous != null) {
            System.err.println("Ahhhhhh, clash of TEIDs");
        }

        final var sender = new Thread(() -> {
            try {
                System.err.println("Starting a new GTPU tunnel");
                final var tunnel = gtpu.establishTunnel(Optional.of("34.226.194.235"), session).toCompletableFuture().get();
                System.err.println("==============================");
                System.err.println("====== Tunnel established. Local IP =====");
                System.err.println(tunnel.getIPv4Address());
                System.err.println("==============================");
                System.err.println("Sleeping 6 seconds");
                Thread.sleep(6000);
                System.err.println("Started, sending");
                tunnel.send("8.8.8.8", 53, dnsQuery);
                System.err.println("Started, sending");
            } catch (final Throwable t) {
                t.printStackTrace();
            }
        });
        sender.start();
    }
}
