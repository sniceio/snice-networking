package io.snice.networking.gtp.impl;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.gtp.GtpControlTunnel;
import io.snice.networking.gtp.GtpStack;
import io.snice.networking.gtp.PdnSession;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultGtpControlTunnel<C extends GtpAppConfig> implements GtpControlTunnel {

    private final ConnectionId connectionId;
    private final GtpStack<C> stack;

    public static <C extends GtpAppConfig> DefaultGtpControlTunnel of(final ConnectionId connectionId, final GtpStack<C> stack) {
        assertNotNull(connectionId, "The connection id cannot be null");
        assertNotNull(stack, "The GTP Stack cannot be null");
        return new DefaultGtpControlTunnel(connectionId, stack);
    }

    private DefaultGtpControlTunnel(final ConnectionId connectionId, final GtpStack<C> stack) {
        this.connectionId = connectionId;
        this.stack = stack;
    }

    @Override
    public void send(final GtpEvent msg) {
        assertArgument(msg.isMessageWriteEvent(), "You can only send " + GtpMessageWriteEvent.class.getName());
        stack.send(msg.toMessageWriteEvent());
    }

    @Override
    public void send(final GtpMessage msg) {
        stack.send(msg, connectionId);
    }

    @Override
    public PdnSession.Builder createPdnSession(final String imsi) {
        // TODO: SNICE-26: get the NAT:ed address from the vip address of the NIC.
        return DefaultPdnSession.createNewSession(this, imsi);
        // final var natedAddress = "52.202.165.16";
        // final var csr = createCsr(natedAddress);
        // final var evt = GtpMessageWriteEvent.of(csr, actualConnection);
        // outstandingRequests.putIfAbsent(csr.getHeader().getSequenceNo(), csr);
        // actualConnection.send(evt);
        // return null;
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
    public boolean connect() {
        return true;
    }

    @Override
    public void close() {
        stack.close(connectionId);
    }

}
