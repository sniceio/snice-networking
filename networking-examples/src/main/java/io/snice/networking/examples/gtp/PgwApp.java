package io.snice.networking.examples.gtp;

import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2MessageType;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.path.EchoRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Imsi;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Msisdn;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Recovery;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;
import io.snice.networking.gtp.GtpApplication;
import io.snice.networking.gtp.GtpEnvironment;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

public class PgwApp extends GtpApplication<GtpConfig> {

    @Override
    public void initialize(final NetworkBootstrap<Connection<GtpEvent>, GtpEvent, GtpConfig> bootstrap) {

        bootstrap.onConnection(c -> true).accept(b -> {
            b.match(GtpEvent::isCreateSessionRequest).map(GtpEvent::toGtp2Message).consume(PgwApp::processCreateSessionRequest);
            b.match(GtpEvent::isCreateSessionResponse).map(GtpEvent::toGtp2Message).consume(PgwApp::processCreateSessionResponse);
            b.match(GtpEvent::isEchoRequest).map(GtpEvent::toGtp2Message).consume(PgwApp::processEchoRequest);
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
        System.err.println("Yay, got an ECHO");
        final var echo = (EchoRequest) message;
        final var echoResponse = echo.createResponse().withTliv(Recovery.ofValue("7")).build();
        final var evt = GtpMessageWriteEvent.of(echoResponse, connection);
        connection.send(evt);
    }

    @Override
    public void run(final GtpConfig configuration, final GtpEnvironment<GtpConfig> environment) {
        environment.connect(Transport.udp, "3.87.113.62", 2123).thenAccept(c -> {
            final var csr = Gtp2Message.create(Gtp2MessageType.CREATE_SESSION_REQUEST)
                    .withTliv(Imsi.ofValue("001001123456"))
                    .withTliv(Msisdn.ofValue("41555512345"))
                    .build();

            final var evt = GtpMessageWriteEvent.of(csr, c);
            c.send(evt);
        });
    }

    public static void main(final String... args) throws Exception {
        final var pgw = new PgwApp();
        pgw.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/PGW.yml");
    }
}
