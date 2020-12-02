package io.snice.networking.examples.gtp;


import io.snice.buffer.Buffers;
import io.snice.codecs.codec.MccMnc;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Response;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.path.EchoRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Recovery;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Uli;
import io.snice.codecs.codec.gtp.gtpc.v2.type.EcgiField;
import io.snice.codecs.codec.gtp.gtpc.v2.type.RatType;
import io.snice.codecs.codec.gtp.gtpc.v2.type.TaiField;
import io.snice.codecs.codec.gtp.gtpc.v2.type.UliType;
import io.snice.networking.gtp.*;
import io.snice.networking.gtp.event.GtpEvent;

import java.util.concurrent.atomic.AtomicReference;

import static io.snice.codecs.codec.gtp.gtpc.v2.type.PdnType.Type.IPv4;

/**
 * Sample app that ties together both GTP-C and GTP-U
 */
public class Sgw2 extends GtpApplication<GtpConfig> {

    /**
     * Our one and only tunnel, which of course in a real application is un-realistic but for
     * this simple example, we don't care. We'll establish exactly one tunnel and use it for
     * everything.
     */
    private final AtomicReference<GtpControlTunnel> tunnel = new AtomicReference<>();

    @Override
    public void initialize(final GtpBootstrap<GtpConfig> bootstrap) {
        bootstrap.onConnection(c -> true).accept(b -> {
            b.match(GtpEvent::isCreateSessionResponse).map(GtpEvent::toGtp2Message).consume(Sgw2::processCreateSessionResponse);
            b.match(GtpEvent::isDeleteSessionResponse).map(GtpEvent::toGtp2Message).consume(Sgw2::processDeleteSessionResponse);
        });
    }

    private static void processCreateSessionResponse(final GtpTunnel tunnel, final Gtp2Message message) {
        // actually, now with Transaction support, we don't "go" this way anymore...
    }

    private static void processDeleteSessionResponse(final GtpTunnel tunnel, final Gtp2Message message) {
        // TODO - may also optionally in the future belong to a session...
    }

    // TODO: needs to move into a Uli convenience builder...
    private static Uli createUli() {
        final var tac = Buffers.wrap((byte) 0x02, (byte) 0x01);
        final var tai = TaiField.of(MccMnc.of("901", "62"), tac);
        final var eci = Buffers.wrap((byte) 0x00, (byte) 0x11, (byte) 0xAA, (byte) 0xBB);
        final var ecgi = EcgiField.of(MccMnc.of("901", "62"), eci);
        return Uli.ofValue(UliType.create().withTai(tai).withEcgi(ecgi).build());
    }

    @Override
    public void run(final GtpConfig configuration, final GtpEnvironment<GtpConfig> environment) {
        final var pgw = "3.81.248.57";
        final var sgw = "107.20.226.156";
        final var imsi = "999994000000642";
        final var csr = CreateSessionRequest.create()
                .withTeid(Teid.ZEROS)
                .withRat(RatType.EUTRAN)
                .withAggregateMaximumBitRate(10000, 10000)
                .withImsi(imsi)
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
            tunnel.set(c);
            try {
                c.createNewTransaction(csr)
                        .onAnswer(this::sendDeleteSession)
                        .withApplicationData("hello world data")
                        .start();
            } catch (final Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * Just wait a little while, then kill the session again... and we'll do it from a different thread to
     * allow other things to come in since currently (will change) it's the same thread pool that used for
     * networking...
     */
    private void sendDeleteSession(final Transaction t, final Gtp2Response response) {
        final var context = PdnSessionContext.of(t.getRequest().toCreateSessionRequest(), response);
        final var thread = new Thread(() -> {
            try {
                Thread.sleep(4000);
                final var delete = context.createDeleteSessionRequest();

                // For "fun", not sending this one in a transaction...
                // tunnel.get().createNewTransaction(delete).onAnswer((tr, r) -> System.err.println("Got the DSR back")).start();

                tunnel.get().send(delete);
            } catch (final Throwable e) {

            }

        });
        thread.start();
    }


    public static void main(final String... args) throws Exception {
        final var sgw = new Sgw2();
        sgw.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/sgw.yml");
    }
}
