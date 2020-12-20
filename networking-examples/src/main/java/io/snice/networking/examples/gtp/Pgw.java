package io.snice.networking.examples.gtp;


import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.DeleteSessionRequest;
import io.snice.networking.gtp.*;
import io.snice.networking.gtp.event.GtpEvent;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Sample app that pretends to be a PGW (pretends because it is far from a complete PGW!)
 */
public class Pgw extends GtpApplication<GtpConfig> {

    /**
     * Our one and only tunnel, which of course in a real application is un-realistic but for
     * this simple example, we don't care. We'll establish exactly one tunnel and use it for
     * everything.
     */
    private final AtomicReference<GtpControlTunnel> tunnel = new AtomicReference<>();
    private GtpEnvironment<GtpConfig> environment;

    private final Sgi sgi;

    public Pgw(final Sgi sgi) {
        this.sgi = sgi;
    }

    /**
     * dns query for google.com. Grabbed from wireshark
     */
    final static Buffer dnsQuery = Buffer.of(
            (byte) 0x5c, (byte) 0x79, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x77, (byte) 0x77, (byte) 0x77,
            (byte) 0x06, (byte) 0x67, (byte) 0x6f, (byte) 0x6f, (byte) 0x67, (byte) 0x6c, (byte) 0x65, (byte) 0x03,
            (byte) 0x63, (byte) 0x6f, (byte) 0x6d, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01);

    @Override
    public void initialize(final GtpBootstrap<GtpConfig> bootstrap) {
        bootstrap.onConnection(c -> true).accept(b -> {
            b.match(GtpEvent::isPdu).map(GtpEvent::toGtp1Message).consume(sgi::processPdu);
            b.match(GtpEvent::isCreateSessionRequest).map(GtpEvent::toCreateSessionRequest).consume(Pgw::processCreateSessionRequest);
            b.match(GtpEvent::isDeleteSessionRequest).map(GtpEvent::toDeleteSessionRequest).consume(Pgw::processDeleteSessionRequest);
        });
    }

    private static void processCreateSessionRequest(final GtpTunnel tunnel, final CreateSessionRequest request) {
        final var response = request.createResponse()
                .withIPv4PdnAddressAllocation("20.30.40.50")
                .withNewSenderControlPlaneFTeid()
                .withRandomizedTeid()
                .withIPv4Address("127.0.0.1")
                .doneFTeid()
                .withNewBearerContext()
                .withEpsBearerId(5)
                .withNewSgwFTeid()
                .withRandomizedTeid()
                .withIPv4Address("127.0.0.1")
                .doneFTeid()
                .withNewBearerQualityOfService(9)
                .withPriorityLevel(10)
                .withPci()
                .doneBearerQoS()
                .doneBearerContext()
                .build();

        tunnel.send(response);
    }

    private static void processDeleteSessionRequest(final GtpTunnel tunnel, final DeleteSessionRequest request) {
        final var response = request.createResponse().build();
        tunnel.send(response);
    }

    private static void processCreateSessionResponse(final GtpTunnel tunnel, final Gtp2Message message) {
        // actually, now with Transaction support, we don't "go" this way anymore...
        final var response = message.toGtp2Message().toCreateSessionRequest().createResponse()
                .withImsi("asdf").build();
        tunnel.send(response);
    }

    private static void processDeleteSessionResponse(final GtpTunnel tunnel, final Gtp2Message message) {
        // if we are running with Session support, this one will not be called.
    }


    @Override
    public void run(final GtpConfig configuration, final GtpEnvironment<GtpConfig> environment) {
        this.environment = environment;
    }

    public static void main(final String... args) throws Exception {
        final var sgi = new Sgi();
        final var pgw = new Pgw(sgi);
        sgi.run("SgiServerConfig.yml");
        pgw.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/pgw.yml");

        // run with this example config if you are running this PGW behind the proxy example
        // app and both running on the same localhost.
        // pgw.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/pgw_behind_proxy.yml");
    }
}
