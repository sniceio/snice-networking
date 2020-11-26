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
import io.snice.networking.gtp.GtpStack;
import io.snice.networking.gtp.GtpUserTunnel;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.conf.GtpConfig;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;
import io.snice.preconditions.PreConditions;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultGtpUserTunnel<C extends GtpAppConfig> implements GtpUserTunnel {

    private final ConnectionId connectionId;
    private final GtpStack<C> stack;

    public static <C extends GtpAppConfig> GtpUserTunnel of(final ConnectionId connectionId, final GtpStack<C> stack) {
        assertNotNull(connectionId, "The connection id cannot be null");
        assertNotNull(stack, "The GTP Stack cannot be null");
        return new DefaultGtpUserTunnel(connectionId, stack);
    }

    private DefaultGtpUserTunnel(final ConnectionId connectionId, final GtpStack stack) {
        this.connectionId = connectionId;
        this.stack = stack;

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
    public void send(final GtpMessage msg) {
        stack.send(msg, connectionId);
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
        return connectionId;
    }

    @Override
    public Optional<URI> getVipAddress() {
        throw new RuntimeException("Re-structuring things");
    }

    @Override
    public int getLocalPort() {
        return connectionId.getLocalPort();
    }

    @Override
    public int getDefaultPort() {
        throw new RuntimeException("Re-structuring things");
    }

    @Override
    public byte[] getRawLocalIpAddress() {
        return connectionId.getRawLocalIpAddress();
    }

    @Override
    public String getLocalIpAddress() {
        return connectionId.getLocalIpAddress();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return connectionId.getLocalAddress();
    }

    @Override
    public Buffer getLocalIpAddressAsBuffer() {
        return connectionId.getLocalIpAddressAsBuffer();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return connectionId.getRemoteAddress();
    }

    @Override
    public int getRemotePort() {
        return connectionId.getRemotePort();
    }

    @Override
    public byte[] getRawRemoteIpAddress() {
        return connectionId.getRawRemoteIpAddress();
    }

    @Override
    public String getRemoteIpAddress() {
        return connectionId.getRemoteIpAddress();
    }

    @Override
    public Buffer getRemoteIpAddressAsBuffer() {
        return connectionId.getRemoteIpAddressAsBuffer();
    }

    @Override
    public Transport getTransport() {
        return connectionId.getProtocol();
    }

    @Override
    public void send(final GtpEvent msg) {
        assertArgument(msg.isMessageWriteEvent(), "You can only send " + GtpMessageWriteEvent.class.getName());
        stack.send(msg.toMessageWriteEvent());
    }

    @Override
    public boolean connect() {
        // All tunnels, once constructed, are always connected since it is UDP after all
        // so there is no "connection" per se.
        return true;
    }

    @Override
    public void close() {
        stack.close(connectionId);
    }

}
