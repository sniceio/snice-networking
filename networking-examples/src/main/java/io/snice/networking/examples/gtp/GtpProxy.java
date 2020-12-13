package io.snice.networking.examples.gtp;


import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.DeleteSessionRequest;
import io.snice.networking.gtp.*;
import io.snice.networking.gtp.event.GtpEvent;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Sample app that acts as a GTP Proxy. It will do TEID-NAT:ing and re-write any GTP-C FTEIDs to be
 * pointing to this proxy. Note, this proxy will NOT re-write the GTP-U bearers and as such, there should
 * be no PDU packets flowing through this proxy.
 */
public class GtpProxy extends GtpApplication<GtpProxyConfig> {

    private GtpEnvironment<GtpProxyConfig> environment;

    @Override
    public void initialize(final GtpBootstrap<GtpProxyConfig> bootstrap) {
        bootstrap.onConnection(c -> true).accept(b -> {
            b.match(GtpEvent::isCreateSessionRequest).map(GtpEvent::toCreateSessionRequest).consume(GtpProxy::processCreateSessionRequest);
            b.match(GtpEvent::isDeleteSessionRequest).map(GtpEvent::toDeleteSessionRequest).consume(GtpProxy::processDeleteSessionRequest);
        });
    }
    private static void processCreateSessionRequest(final GtpTunnel tunnel, final CreateSessionRequest request) {
        final var response = request.createResponse().build();
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
    public void run(final GtpProxyConfig configuration, final GtpEnvironment<GtpProxyConfig> environment) {
        this.environment = environment;
    }

    public static void main(final String... args) throws Exception {
        final var proxy = new GtpProxy();
        proxy.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/gtp_proxy.yml");
    }
}
