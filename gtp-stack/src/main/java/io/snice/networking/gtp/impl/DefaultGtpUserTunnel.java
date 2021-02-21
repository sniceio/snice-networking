package io.snice.networking.gtp.impl;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.gtp.DataTunnel;
import io.snice.networking.gtp.EpsBearer;
import io.snice.networking.gtp.GtpUserTunnel;
import io.snice.networking.gtp.PdnSessionContext;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultGtpUserTunnel<C extends GtpAppConfig> implements InternalGtpUserTunnel {

    private final ConnectionId connectionId;
    private final InternalGtpStack<C> stack;

    public static <C extends GtpAppConfig> GtpUserTunnel of(final ConnectionId connectionId, final InternalGtpStack<C> stack) {
        assertNotNull(connectionId, "The connection id cannot be null");
        assertNotNull(stack, "The GTP Stack cannot be null");
        return new DefaultGtpUserTunnel(connectionId, stack);
    }

    private DefaultGtpUserTunnel(final ConnectionId connectionId, final InternalGtpStack<C> stack) {
        this.connectionId = connectionId;
        this.stack = stack;
    }

    @Override
    public void send(final GtpMessage msg) {
        stack.send(msg, this);
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

    @Override
    public <T> DataTunnel.Builder<T> createDataTunnel(final Class<T> type, final String remoteHost, final int port) {
        return stack.createDataTunnel(this, type, remoteHost, port);
    }

    @Override
    public EpsBearer createBearer(final PdnSessionContext ctx, final int localPort) {
        return EpsBearer.create(this, ctx, localPort);
    }
}
