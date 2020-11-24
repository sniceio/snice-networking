package io.snice.networking.gtp.impl;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.gtp.GtpControlTunnel;
import io.snice.networking.gtp.PdnSession;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultGtpControlTunnel implements GtpControlTunnel {

    private final Connection<GtpEvent> actualConnection;

    private final ConcurrentMap<Buffer, Gtp2Request> outstandingRequests = new ConcurrentHashMap<>();


    public static DefaultGtpControlTunnel of(final Connection<GtpEvent> actualConnection) {
        assertNotNull(actualConnection, "The underlying connection cannot be null");
        return new DefaultGtpControlTunnel(actualConnection);
    }

    private DefaultGtpControlTunnel(final Connection<GtpEvent> actualConnection) {
        this.actualConnection = actualConnection;
    }

    @Override
    public void send(final GtpEvent msg) {
        actualConnection.send(msg);
    }

    @Override
    public void send(final GtpMessage msg) {
        final var writeEvent = GtpMessageWriteEvent.of(msg, actualConnection);
        actualConnection.send(writeEvent);
    }

    @Override
    public PdnSession.Builder createPdnSession(final String imsi) {
        // TODO: SNICE-26: get the NAT:ed address from the vip address of the NIC.
        return DefaultPdnSession.createNewSession(imsi);
        // final var natedAddress = "52.202.165.16";
        // final var csr = createCsr(natedAddress);
        // final var evt = GtpMessageWriteEvent.of(csr, actualConnection);
        // outstandingRequests.putIfAbsent(csr.getHeader().getSequenceNo(), csr);
        // actualConnection.send(evt);
        // return null;
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
    public boolean connect() {
        return actualConnection.connect();
    }

    @Override
    public void close() {
        actualConnection.close();
    }

}
