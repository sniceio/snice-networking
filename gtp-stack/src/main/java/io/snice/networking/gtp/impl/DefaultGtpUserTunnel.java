package io.snice.networking.gtp.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1MessageType;
import io.snice.codecs.codec.gtp.gtpc.v1.impl.ImmutableGtp1Message;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Paa;
import io.snice.codecs.codec.transport.UdpMessage;
import io.snice.networking.common.Connection;
import io.snice.networking.gtp.Bearer;
import io.snice.networking.gtp.GtpUserTunnel;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultGtpUserTunnel implements GtpUserTunnel {

    public static DefaultGtpUserTunnel of(final Connection<GtpEvent> connection, final Paa paa, final Bearer localBearer, final Bearer remoteBearer) {
        assertNotNull(connection, "The connection cannot be null");
        assertNotNull(paa, "The PAA cannot be null");
        assertNotNull(localBearer, "The local Bearer cannot be null");
        assertNotNull(remoteBearer, "The local Bearer cannot be null");
        return new DefaultGtpUserTunnel(connection, paa, localBearer, remoteBearer);
    }

    private final Connection<GtpEvent> connection;
    private final Paa paa;
    private final Bearer localBearer;
    private final Bearer remoteBearer;

    private DefaultGtpUserTunnel(final Connection<GtpEvent> connection, final Paa paa, final Bearer localBearer, final Bearer remoteBearer) {
        this.connection = connection;
        this.paa = paa;
        this.localBearer = localBearer;
        this.remoteBearer = remoteBearer;
    }

    @Override
    public String getIPv4Address() {
        return paa.getValue().getIPv4Address().map(b -> b.toIPv4String(0)).get();
    }

    @Override
    public void send(final String remoteIp, final int remotePort, final String msg) {
        send(remoteIp, remotePort, Buffers.wrap(msg));
    }

    @Override
    public void send(final String remoteIp, final int remotePort, final Buffer data) {

        final var ipv4 = UdpMessage.createUdpIPv4(data)
                .withDestinationPort(remotePort)
                .withSourcePort(9899)
                .withTTL(64)
                .withDestinationIp(remoteIp)
                .withSourceIp(paa.getValue().getIPv4Address().get())
                .build();

        final var gtpU = ImmutableGtp1Message.create(Gtp1MessageType.G_PDU)
                .withTeid(remoteBearer.getTeid())
                .withPayload(ipv4.getBuffer())
                // .withRandomSeqNo()
                .build();
        final var evt = GtpMessageWriteEvent.of(gtpU, connection);
        connection.send(evt);
    }


    @Override
    public Bearer getDefaultLocalBearer() {
        return localBearer;
    }

    @Override
    public Bearer getDefaultRemoteBearer() {
        return remoteBearer;
    }
}
