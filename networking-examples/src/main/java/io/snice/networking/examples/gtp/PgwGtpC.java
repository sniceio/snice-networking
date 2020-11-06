package io.snice.networking.examples.gtp;

import io.snice.buffer.Buffers;
import io.snice.codecs.codec.MccMnc;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2MessageType;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.path.EchoRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.*;
import io.snice.codecs.codec.gtp.gtpc.v2.type.*;
import io.snice.codecs.codec.tgpp.ReferencePoint;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;
import io.snice.networking.gtp.GtpApplication;
import io.snice.networking.gtp.GtpEnvironment;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

public class PgwGtpC extends GtpApplication<GtpConfig> {

    @Override
    public void initialize(final NetworkBootstrap<Connection<GtpEvent>, GtpEvent, GtpConfig> bootstrap) {

        bootstrap.onConnection(c -> true).accept(b -> {
            b.match(GtpEvent::isCreateSessionRequest).map(GtpEvent::toGtp2Message).consume(PgwGtpC::processCreateSessionRequest);
            b.match(GtpEvent::isCreateSessionResponse).map(GtpEvent::toGtp2Message).consume(PgwGtpC::processCreateSessionResponse);
            b.match(GtpEvent::isEchoRequest).map(GtpEvent::toGtp2Message).consume(PgwGtpC::processEchoRequest);
            b.match(evt -> true).consume((c, gtp) -> {
                System.err.println("Received something else: " + gtp.toMessageEvent().getMessage());
            });
        });
    }

    private static void processCreateSessionRequest(final Connection<GtpEvent> connection, final Gtp2Message msg) {
        System.err.println("yay, got a response");
    }

    private static void processCreateSessionResponse(final Connection<GtpEvent> connection, final Gtp2Message message) {
        System.err.println("yay, got a Create Session Response");
    }

    private static void processEchoRequest(final Connection<GtpEvent> connection, final Gtp2Message message) {
        // TODO: will re-do this. Will expose a GtpTunnel instead and hide the creating of these
        // write events etc.
        final var echo = (EchoRequest) message;
        final var echoResponse = echo.createResponse().withTliv(Recovery.ofValue("7")).build();
        final var evt = GtpMessageWriteEvent.of(echoResponse, connection);
        connection.send(evt);
    }

    @Override
    public void run(final GtpConfig configuration, final GtpEnvironment<GtpConfig> environment) {
        final var arp = ArpType.ofValue(7, true, true);
        final var qos = QosType.ofQci(2).build();
        final var bqos = BearerQos.ofValue(BearerQosType.ofValue(arp, qos));

        final var ftiedType = FTeidType.create()
                .withIPv4Address("192.168.0.100")
                .withReferencePoint(ReferencePoint.S5, true)
                .withTeid(Teid.ZEROS)
                .build();

        final var ftied = FTeid.ofValue(FTeidType.create()
                .withIPv4Address("192.168.0.100")
                .withReferencePoint(ReferencePoint.S5, true)
                .withTeid(Teid.ZEROS)
                .build());

        final var ebi = Ebi.ofValue(EbiType.ofValue(5));
        final var grouped = GroupedType.ofValue(bqos, ftied, ebi);

        final var bearerContext = BearerContext.ofValue(grouped);

        final var tac = Buffers.wrap((byte) 0x11, (byte) 0x22);
        final var tai = TaiField.of(MccMnc.of("123", "12"), tac);
        final var uli = UliType.create().withTai(tai).build();

        final var servingNetwork = ServingNetwork.ofValue(MccMncType.ofValue("310/410"));
        final var ambr = Ambr.ofValue(AmbrType.ofValue(10000, 20000));

        System.err.println("===== starting to run ====");
        environment.connect(Transport.udp, "3.89.25.140", 2123).thenAccept(c -> {

            System.err.println("Whats going on?");

            final var f = FTeid.ofValue(ftiedType);

            final var csr = Gtp2Message.create(Gtp2MessageType.CREATE_SESSION_REQUEST)
                    .withTeid(Teid.of(Buffers.wrap((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00)))
                    .withTliv(f)
                    // TODO: fix grouped. And fix all IE of type grouped. Like I did for diameter
                    .withTliv(Uli.ofValue(uli))
                    .withTliv(bearerContext)
                    .withTliv(servingNetwork)
                    .withTliv(ambr)
                    .withTliv(ApnRestriction.ofValue(CounterType.parse("0")))
                    .withTliv(Paa.ofValue(PaaType.fromIPv4("10.11.12.13")))
                    .withTliv(SelectionMode.ofValue(SelectionModeType.ofValue(0)))
                    .withTliv(Apn.ofValue("super"))
                    .withTliv(Pdn.ofValue(PdnType.of(PdnType.Type.IPv4)))
                    .withTliv(Mei.ofValue(TbcdType.parse("123456789")))
                    .withTliv(Rat.ofValue("6"))
                    .withTliv(Imsi.ofValue("999994000000642"))
                    .withTliv(Msisdn.ofValue("41555512345"))
                    .build();

            final var evt = GtpMessageWriteEvent.of(csr, c);
            c.send(evt);
        });
    }

    public static void main(final String... args) throws Exception {
        final var pgw = new PgwGtpC();
        pgw.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/pgw_gtpc.yml");
    }
}
