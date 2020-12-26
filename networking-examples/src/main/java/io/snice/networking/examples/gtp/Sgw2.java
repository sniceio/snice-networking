package io.snice.networking.examples.gtp;


import io.hektor.core.Hektor;
import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.MccMnc;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Uli;
import io.snice.codecs.codec.gtp.gtpc.v2.type.EcgiField;
import io.snice.codecs.codec.gtp.gtpc.v2.type.RatType;
import io.snice.codecs.codec.gtp.gtpc.v2.type.TaiField;
import io.snice.codecs.codec.gtp.gtpc.v2.type.UliType;
import io.snice.codecs.codec.internet.IpMessage;
import io.snice.codecs.codec.internet.ipv4.IPv4Message;
import io.snice.codecs.codec.transport.UdpMessage;
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
    private GtpEnvironment<GtpConfig> environment;
    private Hektor hektor;

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
            b.match(GtpEvent::isPdu).map(GtpEvent::toGtp1Message).consume(Sgw2::processPdu);
            b.match(GtpEvent::isCreateSessionResponse).map(GtpEvent::toGtp2Message).consume(Sgw2::processCreateSessionResponse);
            b.match(GtpEvent::isDeleteSessionResponse).map(GtpEvent::toGtp2Message).consume(Sgw2::processDeleteSessionResponse);
        });

    }

    private static void processPdu(final GtpTunnel tunnel, final Gtp1Message pdu) {

        // TODO: some convenience methods for dealing with IP packets would be nice...
        final var payload = pdu.getPayload().get();
        final IPv4Message<Buffer> ipv4 = IpMessage.frame(payload).toIPv4();
        final var udp = UdpMessage.frame(ipv4.getPayload());
        final var content = udp.getPayload();

        // now do something with the content.
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
        this.environment = environment;
        final var hektorConfig = environment.getConfig().getHektorConfig();
        hektor = Hektor.withName("sgw").withConfiguration(hektorConfig).build();

        // If the PGW is behind a NAT, make sure you grab the public address (duh)
        final var pgw = "127.0.0.1";

        // If you're behind a NAT, you want the NAT:ed address here. Otherwise, your
        // local NIC is fine. All depends where the PGW is...
        final var sgw = "127.0.0.1";
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

        environment.initiateNewPdnSession(csr)
                .withRemoteIPv4(pgw)
                .start()
                .thenAccept(session -> {
                    // Note: kicking off a new thread because it is the same thread pool
                    // as the underlying stack.
                    final var t = new Thread(() -> {
                        try {
                            Thread.sleep(100);
                            final var bearer = session.establishDefaultBearer().toCompletableFuture().get();
                            // bearer.send("165.227.89.76", 52483, "hello world");
                            bearer.send("8.8.8.8", 53, dnsQuery);
                            Thread.sleep(4000);
                            session.terminate();
                        } catch (final Throwable e) {
                            e.printStackTrace();
                        }
                    });
                    t.start();
                });
    }

    public static void main(final String... args) throws Exception {
        final var sgw = new Sgw2();
        sgw.run("sgw.yml");
    }
}
