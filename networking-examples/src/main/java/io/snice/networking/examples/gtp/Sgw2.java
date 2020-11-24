package io.snice.networking.examples.gtp;


import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.path.EchoRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Recovery;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.gtp.GtpApplication;
import io.snice.networking.gtp.GtpEnvironment;
import io.snice.networking.gtp.GtpTunnel;
import io.snice.networking.gtp.event.GtpEvent;

/**
 * Sample app that ties together both GTP-C and GTP-U
 */
public class Sgw2 extends GtpApplication<GtpConfig> {

    @Override
    public void initialize(final NetworkBootstrap<GtpTunnel, GtpEvent, GtpConfig> bootstrap) {
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

    @Override
    public void run(final GtpConfig configuration, final GtpEnvironment<GtpConfig> environment) {
        final var pgw = "3.83.40.41";
        System.err.println("Establishing new tunnel");
        environment.establishControlPlane(pgw, 2123).thenAccept(c -> {
            System.err.println("============= tunnel has been established successfully ==============");
            // final var sessionFuture = c.createPdnSession("99999");
        });
        // environment.establishControlPlane()
    }

    public static void main(final String... args) throws Exception {
        final var sgw = new Sgw2();
        sgw.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/sgw.yml");
    }
}
