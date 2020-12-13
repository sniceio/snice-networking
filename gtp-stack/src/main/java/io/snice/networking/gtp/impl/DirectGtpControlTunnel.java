package io.snice.networking.gtp.impl;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.gtp.GtpApplication;
import io.snice.networking.gtp.GtpUserTunnel;
import io.snice.networking.gtp.IllegalGtpMessageException;
import io.snice.networking.gtp.Transaction;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * This {@link DirectGtpControlTunnel} is a thin wrapper under the actual {@link Connection} as given to us
 * from the underlying Snice Networking stack. However, for those building applications using {@link GtpApplication}
 * we want to be in more control and as such, the {@link GtpApplication} will trap all of those "real" connections
 * and give out one that doesn't have direct access to the Netty Pipeline but rather is forced to go through
 * the {@link GtpApplication}.
 * <p>
 * As such, to the user, we will only present {@link DefaultGtpControlTunnel}s and {@link GtpUserTunnel}s
 * which will then just call {@link InternalGtpStack#send(GtpMessage, InternalGtpUserTunnel)} etc.
 */
public class DirectGtpControlTunnel<C extends GtpAppConfig> implements InternalGtpControlTunnel {

    private final Connection<GtpEvent> actualConnection;
    private final InternalGtpStack<C> stack;

    public static <C extends GtpAppConfig> DirectGtpControlTunnel of(final Connection<GtpEvent> actualConnection, final InternalGtpStack<C> stack) {
        assertNotNull(actualConnection);
        assertNotNull(stack);
        return new DirectGtpControlTunnel(actualConnection, stack);
    }

    private DirectGtpControlTunnel(final Connection<GtpEvent> actualConnection, final InternalGtpStack<C> stack) {
        this.actualConnection = actualConnection;
        this.stack = stack;
    }

    @Override
    public void send(final GtpEvent msg) {
        actualConnection.send(msg);
    }

    @Override
    public void send(final GtpMessage msg) {
        actualConnection.send(GtpMessageWriteEvent.of(msg, actualConnection.id()));
    }

    @Override
    public Transaction.Builder createNewTransaction(final Gtp2Request request) throws IllegalGtpMessageException {
        return stack.createNewTransaction(this, request);
    }

    @Override
    public ConnectionId id() {
        return actualConnection.id();
    }

    @Override
    public Optional<URI> getVipAddress() {
        throw new RuntimeException("Re-structuring things");
    }

    @Override
    public int getLocalPort() {
        return actualConnection.getLocalPort();
    }

    @Override
    public int getDefaultPort() {
        throw new RuntimeException("Re-structuring things");
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
        return true;
    }

    @Override
    public void close() {
        stack.close(actualConnection.id());
    }
}