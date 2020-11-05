package io.snice.networking.examples.gtp;

import io.snice.buffer.Buffers;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2MessageType;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.path.EchoRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.*;
import io.snice.codecs.codec.gtp.gtpc.v2.type.FTeidType;
import io.snice.codecs.codec.gtp.gtpc.v2.type.PdnType;
import io.snice.codecs.codec.gtp.gtpc.v2.type.TbcdType;
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
        environment.connect(Transport.udp, "3.88.129.158", 2123).thenAccept(c -> {
            final var ftied = FTeidType.create()
                    .withIPv4Address("192.168.0.100")
                    .withReferencePoint(ReferencePoint.S5, true)
                    .withTeid(Teid.ZEROS)
                    .build();

            final var f = FTeid.ofValue(ftied);

            final var csr = Gtp2Message.create(Gtp2MessageType.CREATE_SESSION_REQUEST)
                    .withTeid(Teid.of(Buffers.wrap((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00)))
                    .withTliv(f)
                    .withTliv(Apn.ofValue("super.hello"))
                    .withTliv(Pdn.ofValue(PdnType.of(PdnType.Type.IPv4)))
                    .withTliv(Mei.ofValue(TbcdType.parse("123456789")))
                    .withTliv(Rat.ofValue("6"))
                    .withTliv(Imsi.ofValue("001001123456"))
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
