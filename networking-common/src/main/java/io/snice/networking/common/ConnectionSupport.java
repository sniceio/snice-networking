package io.snice.networking.common;

import io.snice.buffer.Buffer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;

/**
 * There are many cases when a particular network protocol want to wrap the actual
 * connection and provide a different send method, or perhaps transform the object
 * being sent before it is written to the channel context. As such, you may then find
 * yourself wrapping the "actual connection" and then delegating most of the method
 * calls to it, which is annoying and boring to write, hence this supporting class.
 */
public abstract class ConnectionSupport<T> implements Connection<T> {

    protected final Connection<T> actualConnection;

    protected ConnectionSupport(final Connection<T> actualConnection) {
        this.actualConnection = actualConnection;
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
