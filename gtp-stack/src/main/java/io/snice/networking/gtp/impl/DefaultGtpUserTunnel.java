package io.snice.networking.gtp.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1MessageType;
import io.snice.codecs.codec.gtp.gtpc.v1.impl.ImmutableGtp1Message;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Paa;
import io.snice.codecs.codec.transport.UdpMessage;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.gtp.Bearer;
import io.snice.networking.gtp.GtpUserTunnel;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultGtpUserTunnel implements GtpUserTunnel {

    private final Connection<GtpEvent> actualConnection;

    public static DefaultGtpUserTunnel of(final Connection<GtpEvent> connection, final Paa paa, final Bearer localBearer, final Bearer remoteBearer) {
        throw new RuntimeException("Dont use this one anymore");
        // assertNotNull(connection, "The connection cannot be null");
        // assertNotNull(paa, "The PAA cannot be null");
        // assertNotNull(localBearer, "The local Bearer cannot be null");
        // assertNotNull(remoteBearer, "The local Bearer cannot be null");
        // return new DefaultGtpUserTunnel(connection, paa, localBearer, remoteBearer);
    }

    public static GtpUserTunnel of(final Connection<GtpEvent> actualConnection) {
        assertNotNull(actualConnection, "The underlying connection cannot be null");
        return DefaultGtpUserTunnel.of(actualConnection);
    }

    private DefaultGtpUserTunnel(final Connection<GtpEvent> actualConneciton) {
        this.actualConnection = actualConneciton;
        this.paa = null;
        this.localBearer = null;
        this.remoteBearer = null;
    }

    private final Paa paa;
    private final Bearer localBearer;
    private final Bearer remoteBearer;

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
        send(gtpU);
    }

    @Override
    public void send(GtpMessage msg) {
        final var evt = GtpMessageWriteEvent.of(msg, actualConnection);
        actualConnection.send(evt);
    }


    @Override
    public Bearer getDefaultLocalBearer() {
        return localBearer;
    }

    @Override
    public Bearer getDefaultRemoteBearer() {
        return remoteBearer;
    }

    @Override
    public ConnectionId id() {
        return actualConnection.id();
    }

    @Override
    public Optional<URI> getVipAddress() {
        return actualConnection.getVipAddress();
    }

    @Override
    public int getLocalPort() {
        return actualConnection.getLocalPort();
    }

    @Override
    public int getDefaultPort() {
        return actualConnection.getDefaultPort();
    }

    @Override
    public byte[] getRawLocalIpAddress() {
        return actualConnection.getRawLocalIpAddress();
    }

    @Override
    public String getLocalIpAddress() {
        return actualConnection.getLocalIpAddress();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return actualConnection.getLocalAddress();
    }

    @Override
    public Buffer getLocalIpAddressAsBuffer() {
        return actualConnection.getLocalIpAddressAsBuffer();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return actualConnection.getRemoteAddress();
    }

    @Override
    public int getRemotePort() {
        return actualConnection.getRemotePort();
    }

    @Override
    public byte[] getRawRemoteIpAddress() {
        return actualConnection.getRawRemoteIpAddress();
    }

    @Override
    public String getRemoteIpAddress() {
        return actualConnection.getRemoteIpAddress();
    }

    @Override
    public Buffer getRemoteIpAddressAsBuffer() {
        return actualConnection.getRemoteIpAddressAsBuffer();
    }

    @Override
    public Transport getTransport() {
        return actualConnection.getTransport();
    }

    @Override
    public void send(final GtpEvent msg) {
        actualConnection.send(msg);
    }

    @Override
    public boolean connect() {
        return actualConnection.connect();
    }

    @Override
    public void close() {
        actualConnection.close();
    }

}
