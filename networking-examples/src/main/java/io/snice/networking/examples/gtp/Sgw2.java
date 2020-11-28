package io.snice.networking.examples.gtp;


import io.snice.buffer.Buffers;
import io.snice.codecs.codec.MccMnc;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.path.EchoRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Recovery;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Uli;
import io.snice.codecs.codec.gtp.gtpc.v2.type.EcgiField;
import io.snice.codecs.codec.gtp.gtpc.v2.type.RatType;
import io.snice.codecs.codec.gtp.gtpc.v2.type.TaiField;
import io.snice.codecs.codec.gtp.gtpc.v2.type.UliType;
import io.snice.networking.gtp.GtpApplication;
import io.snice.networking.gtp.GtpBootstrap;
import io.snice.networking.gtp.GtpEnvironment;
import io.snice.networking.gtp.GtpTunnel;
import io.snice.networking.gtp.event.GtpEvent;

import static io.snice.codecs.codec.gtp.gtpc.v2.type.PdnType.Type.IPv4;

/**
 * Sample app that ties together both GTP-C and GTP-U
 */
public class Sgw2 extends GtpApplication<GtpConfig> {

    @Override
    public void initialize(final GtpBootstrap<GtpConfig> bootstrap) {
        bootstrap.onConnection(c -> true).accept(b -> {
            b.match(GtpEvent::isCreateSessionResponse).map(GtpEvent::toGtp2Message).consume(Sgw2::processCreateSessionResponse);
            b.match(GtpEvent::isEchoRequest).map(GtpEvent::toGtp2Message).consume(Sgw2::processEchoRequest);
        });
    }

    private static void processCreateSessionResponse(final GtpTunnel tunnel, final Gtp2Message message) {
        System.err.println("yay, got a Create Session Response");
    }

    private static void processEchoRequest(final GtpTunnel tunnel, final Gtp2Message message) {
        // TODO: will re-do this. Will expose a GtpTunnel instead and hide the creating of these
        // write events etc.
        final var echo = (EchoRequest) message;
        final var echoResponse = echo.createResponse().withTliv(Recovery.ofValue("7")).build();
        tunnel.send(echoResponse);
    }

    private static Uli createUli() {
        final var tac = Buffers.wrap((byte) 0x02, (byte) 0x01);
        final var tai = TaiField.of(MccMnc.of("901", "62"), tac);
        final var eci = Buffers.wrap((byte) 0x00, (byte) 0x11, (byte) 0xAA, (byte) 0xBB);
        final var ecgi = EcgiField.of(MccMnc.of("901", "62"), eci);
        return Uli.ofValue(UliType.create().withTai(tai).withEcgi(ecgi).build());
    }


    @Override
    public void run(final GtpConfig configuration, final GtpEnvironment<GtpConfig> environment) {
        final var pgw = "3.92.49.45";
        final var sgw = "107.20.226.156";
        System.err.println("Establishing new tunnel");
        final var csr = CreateSessionRequest.create()
                .withTeid(Teid.ZEROS)
                .withRat(RatType.EUTRAN)
                .withAggregateMaximumBitRate(10000, 10000)
                .withImsi("999994000000642")
                .withServingNetwork("310/410")
                .withTliv(createUli())
                .withApnSelectionMode(0)
                .withApn("super")
                .withNoApnRestrictions()
                .withPdnType(IPv4)
                .withIPv4PdnAddressAllocation("0.0.0.0")
                .withNewSenderControlPlaneFTeid()
                .withRandomizedTeid()
                .withIPv4Address(sgw)
                .doneFTeid()
                .withNewBearerContext()
                .withNewSgwFTeid()
                .withRandomizedTeid()
                .withIPv4Address(sgw)
                .doneFTeid()
                .withEpsBearerId(5)
                .withNewBearerQualityOfService(9)
                .withPriorityLevel(10)
                .withPci()
                .doneBearerQoS()
                .doneBearerContext()
                .build();
        environment.establishControlPlane(pgw, 2123).thenAccept(c -> {
            System.err.println("======== sending CSR =====");
            c.send(csr);
        });
        // environment.establishControlPlane()
    }

    public static void main(final String... args) throws Exception {
        final var sgw = new Sgw2();
        sgw.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/sgw.yml");
    }
}
