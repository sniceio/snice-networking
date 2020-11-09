package io.snice.networking.examples.gtp;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.MccMnc;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2MessageType;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.path.EchoRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.*;
import io.snice.codecs.codec.gtp.gtpc.v2.type.*;
import io.snice.codecs.codec.tgpp.ReferencePoint;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;
import io.snice.networking.gtp.GtpApplication;
import io.snice.networking.gtp.GtpEnvironment;
import io.snice.networking.gtp.PdnSession;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

public class PgwGtpC extends GtpApplication<GtpConfig> {

    private final ConcurrentMap<Buffer, Gtp2Request> outstandingRequests = new ConcurrentHashMap<>();
    private final ConcurrentMap<Teid, PdnSession> pdnSessions = new ConcurrentHashMap<>();

    private final AtomicReference<Connection<GtpEvent>> tunnel = new AtomicReference<>();

    @Override
    public void initialize(final NetworkBootstrap<Connection<GtpEvent>, GtpEvent, GtpConfig> bootstrap) {

        bootstrap.onConnection(c -> true).accept(b -> {
            b.match(GtpEvent::isCreateSessionRequest).map(GtpEvent::toGtp2Message).consume(PgwGtpC::processCreateSessionRequest);
            b.match(GtpEvent::isCreateSessionResponse).map(GtpEvent::toGtp2Message).consume(this::processCreateSessionResponse);
            b.match(GtpEvent::isEchoRequest).map(GtpEvent::toGtp2Message).consume(PgwGtpC::processEchoRequest);
            b.match(evt -> true).consume((c, gtp) -> {
                System.err.println("Received something else: " + gtp.toMessageEvent().getMessage());
            });
        });
    }

    private static void processCreateSessionRequest(final Connection<GtpEvent> connection, final Gtp2Message msg) {
        System.err.println("yay, got a response");
    }

    private void processCreateSessionResponse(final Connection<GtpEvent> connection, final Gtp2Message message) {
        System.err.println("yay, got a Create Session Response");
        final var response = message.toGtp2Response();
        final var csr = outstandingRequests.remove(response.getHeader().getSequenceNo());
        if (csr == null) {
            System.err.println("No matching transaction");
            return;
        }

        System.err.println("yay, matched it to CSR: " + csr);
        final var session = PdnSession.of(csr, response);
        final var previous = pdnSessions.putIfAbsent(session.getLocalTeid(), session);
        if (previous != null) {
            System.err.println("Ahhhhhh, clash of TEIDs");
        }

        new Thread(() -> {
            try {
                System.err.println("Starting a new thread to send the Delete Session Request in");
                Thread.sleep(2000);
            } catch (final Throwable t) {
            }
            final var c = tunnel.get();
            System.err.println("Will try and kill the session");
            final var dsr = session.createDeleteSessionRequest();
            final var evt = GtpMessageWriteEvent.of(dsr, c);
            c.send(evt);
            System.err.println("Send the DSR: " + dsr);

        }).start();
    }

    private static void processEchoRequest(final Connection<GtpEvent> connection, final Gtp2Message message) {
        // TODO: will re-do this. Will expose a GtpTunnel instead and hide the creating of these
        // write events etc.
        final var echo = (EchoRequest) message;
        final var echoResponse = echo.createResponse().withTliv(Recovery.ofValue("7")).build();
        final var evt = GtpMessageWriteEvent.of(echoResponse, connection);
        connection.send(evt);
    }

    private static Uli createUli() {
        final var tac = Buffers.wrap((byte) 0x02, (byte) 0x01);
        final var tai = TaiField.of(MccMnc.of("901", "62"), tac);
        final var eci = Buffers.wrap((byte) 0x00, (byte) 0x11, (byte) 0xAA, (byte) 0xBB);
        final var ecgi = EcgiField.of(MccMnc.of("901", "62"), eci);
        return Uli.ofValue(UliType.create().withTai(tai).withEcgi(ecgi).build());
    }

    private static FTeid createFTeidGtpc(final String ipv4Address) {
        final var ftiedTypeGtpC = FTeidType.create()
                .withRandomizedTeid()
                .withIPv4Address(ipv4Address)
                .withReferencePoint(ReferencePoint.S5, true) // true = gtp-c
                .build();

        return FTeid.ofValue(ftiedTypeGtpC, 0);
    }

    private Gtp2Request createCsr(final String gtpcTunnelIPv4) {
        final var arp = ArpType.ofValue(10, true, false);
        final var qos = QosType.ofQci(9).build();
        final var bqos = BearerQos.ofValue(BearerQosType.ofValue(arp, qos));

        final var ftiedTypeGtpU = FTeidType.create()
                .withRandomizedTeid()
                .withIPv4Address(gtpcTunnelIPv4)
                .withReferencePoint(ReferencePoint.S5, false) // false = gtp-u
                .build();

        final var fteidGtpC = createFTeidGtpc(gtpcTunnelIPv4);
        final var fteidGtpU = FTeid.ofValue(ftiedTypeGtpU, 2);

        final var ebi = Ebi.ofValue(EbiType.ofValue(5));
        final var grouped = GroupedType.ofValue(ebi, fteidGtpU, bqos);

        final var uli = createUli();

        final var bearerContext = BearerContext.ofValue(grouped);

        final var servingNetwork = ServingNetwork.ofValue(MccMncType.ofValue("901/62"));
        final var ambr = Ambr.ofValue(AmbrType.ofValue(10000, 50000));

        final var timeZone = UeTimeZone.ofValue(Buffers.wrap((byte) 0x08, (byte) 0x00));

        final var csr = Gtp2Message.create(Gtp2MessageType.CREATE_SESSION_REQUEST)
                .withRandomSeqNo()
                .withTeid(Teid.ZEROS) // for initial CSR, the TEID must be zero
                .withTliv(Imsi.ofValue("999994000000642"))
                .withTliv(Mei.ofValue(TbcdType.parse("1234567890123456")))
                .withTliv(servingNetwork)
                .withTliv(Rat.ofValue("6"))
                .withTliv(fteidGtpC)
                .withTliv(Apn.ofValue("super"))
                .withTliv(SelectionMode.ofValue(SelectionModeType.ofValue(0)))
                .withTliv(Pdn.ofValue(PdnType.of(PdnType.Type.IPv4)))
                .withTliv(Paa.ofValue(PaaType.fromIPv4("0.0.0.0")))
                .withTliv(ambr)
                .withTliv(ApnRestriction.ofValue(CounterType.parse("0")))
                .withTliv(bearerContext)
                .withTliv(timeZone)
                .withTliv(uli)
                .build();

        return csr.toGtp2Request();
    }

    @Override
    public void run(final GtpConfig configuration, final GtpEnvironment<GtpConfig> environment) {

        environment.connect(Transport.udp, "3.88.129.158", 2123).thenAccept(c -> {
            // TODO: any good way of dynamically finding out the NAT:ed address?
            tunnel.set(c);
            final var csr = createCsr("107.20.226.156");
            outstandingRequests.putIfAbsent(csr.getHeader().getSequenceNo(), csr);
            final var evt = GtpMessageWriteEvent.of(csr, c);
            c.send(evt);
        });
    }

    public static void main(final String... args) throws Exception {
        final var pgw = new PgwGtpC();
        pgw.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/pgw_gtpc.yml");
    }
}
