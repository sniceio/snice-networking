package io.snice.networking.examples.gtp;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1MessageType;
import io.snice.codecs.codec.gtp.gtpc.v1.impl.ImmutableGtp1Message;
import io.snice.codecs.codec.transport.UdpMessage;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;
import io.snice.networking.gtp.GtpApplication;
import io.snice.networking.gtp.GtpEnvironment;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

public class PgwGtpU extends GtpApplication<GtpConfig> {

    /**
     * dns query for google.com. Grabbed from wireshark
     */
    final static Buffer dnsQuery = Buffer.of(
            (byte) 0x5c, (byte) 0x79, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x77, (byte) 0x77, (byte) 0x77,
            (byte) 0x06, (byte) 0x67, (byte) 0x6f, (byte) 0x6f, (byte) 0x67, (byte) 0x6c, (byte) 0x65, (byte) 0x03,
            (byte) 0x63, (byte) 0x6f, (byte) 0x6d, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01);

    @Override
    public void initialize(final NetworkBootstrap<Connection<GtpEvent>, GtpEvent, GtpConfig> bootstrap) {
        bootstrap.onConnection(c -> true).accept(b -> {
            b.match(evt -> true).consume((c, gtp) -> {
                System.err.println("Received something else: " + gtp.toMessageEvent().getMessage());
            });
        });
    }

    @Override
    public void run(final GtpConfig configuration, final GtpEnvironment<GtpConfig> environment) {
        environment.connect(Transport.udp, "127.0.0.1", 2154).thenAccept(c -> {
            final var ipv4 = UdpMessage.createUdpIPv4(dnsQuery)
                    .withDestinationPort(53)
                    .withSourcePort(9899)
                    .withTTL(34)
                    .withDestinationIp("10.36.10.10")
                    .withSourceIp("11.12.13.14")
                    .build();

            final var gtpU = ImmutableGtp1Message.create(Gtp1MessageType.G_PDU)
                    .withTeid(Teid.random())
                    .withPayload(ipv4.getBuffer())
                    .withRandomSeqNo()
                    .build();
            final var evt = GtpMessageWriteEvent.of(gtpU, c);
            c.send(evt);
        });
    }
}
