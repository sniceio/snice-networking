package io.snice.networking.examples.gtp;


import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1Message;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.DeleteSessionRequest;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.gtp.*;
import io.snice.networking.gtp.event.GtpEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Sample app that pretends to be a PGW (pretends because it is far from a complete PGW!)
 */
public class Pgw extends GtpApplication<GtpConfig> {

    private static final Logger logger = LoggerFactory.getLogger(GtpApplication.class);

    private GtpEnvironment<GtpConfig> environment;

    private final ConcurrentMap<Teid, PdnSessionContext> pdnSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<ConnectionId, GtpTunnel> tunnels = new ConcurrentHashMap<>();

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
        bootstrap.onConnection(c -> true).save(tunnel -> {
            tunnels.put(tunnel.id(), tunnel);
        }).accept(b -> {
            b.match(GtpEvent::isPdu).map(GtpEvent::toGtp1Message).consume(this::processPdu);
            b.match(GtpEvent::isCreateSessionRequest).map(GtpEvent::toCreateSessionRequest).consume(this::processCreateSessionRequest);
            b.match(GtpEvent::isDeleteSessionRequest).map(GtpEvent::toDeleteSessionRequest).consume(this::processDeleteSessionRequest);
        });
    }

    public void processPdu(final GtpTunnel tunnel, final Gtp1Message pdu) {
        final var localTeid = pdu.getHeader().toGtp1Header().getTeid();
        final var pdnSession = pdnSessions.get(localTeid);
        if (pdnSession == null) {
            logger.warn("Unable to locate PDN Session for TEID {}. Dropping PDU", localTeid);
        } else {

            // NOTE: the reason for not using the GtpTunnel that was passed in to us is that
            // Snice-Networking says you shouldn't use that tunnel after the invocation. As such,
            // we will look-up the real tunnel that we saved away earlier since that one we
            // can use outside of method invocation from the underlying snice-networking layers.
            final var t = tunnels.get(tunnel.id());
            sgi.processPdu(t, pdnSession, pdu);
        }
    }

    private void processCreateSessionRequest(final GtpTunnel tunnel, final CreateSessionRequest request) {
        // Note: will blow up if the Optional is empty. But, this is a sample app so...
        final var remoteFTeid = request.getFTeid().get().getValue();
        final var remoteTeid = remoteFTeid.getTeid();

        final var localTeid = Teid.random();
        final var localBearerTeid = Teid.random();
        // logger.info("Creating new Session with Local Sender TEID {} and Local Bearer TEID {}", localTeid, localBearerTeid);

        final var response = request.createResponse()
                .withTeid(remoteTeid) // the TEID of the remote end has to go in the header.
                .withIPv4PdnAddressAllocation("20.30.40.50")
                .withNewSenderControlPlaneFTeid()
                .withTeid(localTeid) // Our local TEID, which, as you can see, goes in our GTP-C FTeid.
                .withIPv4Address("127.0.0.1")
                .doneFTeid()
                .withNewBearerContext()
                .withEpsBearerId(5)
                .withNewSgwFTeid()
                .withTeid(localBearerTeid) // and this is our
                .withIPv4Address("127.0.0.1")
                .doneFTeid()
                .withNewBearerQualityOfService(9)
                .withPriorityLevel(10)
                .withPci()
                .doneBearerQoS()
                .doneBearerContext()
                .build();

        // TODO: the remote/local TEID etc is really from the perspective of the requester
        // v.s. the receiver. Perhaps this should be renamed. A bit confusing because the remote TEID
        // is actually the one I generated above and as such, it is my local one.
        final var pdnSession = PdnSessionContext.of(request, response);
        final var previous = pdnSessions.putIfAbsent(localTeid, pdnSession);
        pdnSessions.putIfAbsent(localBearerTeid, pdnSession);
        if (previous != null) {
            // not supposed to happen. We have a new CSR for an existing TEID. Clash?
            // NOTE: the raw TEID should not be used like this. It should be in context
            // of the remote endpoint since a TEID is scoped to the other endpoint too
            // so we need to build up a "bigger" key for this.
        } else {
            // again, remember how the local/remote is flipped because the PDN Session isn't really
            // smart enough to see things from our perspective, which is from the PGW in this case.
            // The remote/local is from the initiator, i.e. e.g. the SGW. Will change this.
            // logger.info("New PDN Session with Local TEID {},  Remote TEID {}, " +
            // "Local Bearer {}, Remote Bearer {}", pdnSession.getRemoteTeid(), pdnSession.getLocalTeid(),
            // pdnSession.getDefaultRemoteBearer().getTeid(), pdnSession.getDefaultLocalBearer().getTeid());
        }

        tunnel.send(response);
    }

    private void processDeleteSessionRequest(final GtpTunnel tunnel, final DeleteSessionRequest request) {
        // TODO: should be defensive here. Or at least if this was a real PGW implementation and not an example!
        final var teid = request.getHeader().getTeid().get();
        final var session = pdnSessions.remove(teid);
        final var sessionAgain = pdnSessions.remove(session.getRemoteBearerTeid());

        // yes, actually want to compare references. It is supposed to be the exact
        // same instance...
        if (session != sessionAgain) {
            logger.warn("Odd, the PDN Context under the different TEIDs are different");
        }

        final var response = request.createResponse();
        if (session == null) {
            // if there were no session we should say so in the Cause.
            // for now, let's ignore it.
            logger.info("Unable to find the PDN Session with TEID {} ", teid);
        }
        tunnel.send(response.build());
    }

    @Override
    public void run(final GtpConfig configuration, final GtpEnvironment<GtpConfig> environment) {
        this.environment = environment;
    }

    public static void main(final String... args) throws Exception {
        final var sgi = new Sgi();
        final var pgw = new Pgw(sgi);
        sgi.run("SgiServerConfig.yml");
        pgw.run("pgw.yml");
    }
}
