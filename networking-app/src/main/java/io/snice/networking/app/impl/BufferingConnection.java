package io.snice.networking.app.impl;

import io.netty.channel.ChannelHandlerContext;
import io.snice.buffer.Buffer;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;

public class BufferingConnection<T> implements Connection<T> {

    private final Connection<T> connection;

    private T msgToSend;

    public BufferingConnection(final Connection<T> connection) {
        this.connection = connection;
    }

    @Override
    public ConnectionId id() {
        return connection.id();
    }

    @Override
    public Optional<URI> getVipAddress() {
        return connection.getVipAddress();
    }

    @Override
    public int getLocalPort() {
        return connection.getLocalPort();
    }

    @Override
    public int getDefaultPort() {
        return connection.getDefaultPort();
    }

    @Override
    public byte[] getRawLocalIpAddress() {
        return connection.getRawLocalIpAddress();
    }

    @Override
    public String getLocalIpAddress() {
        return connection.getLocalIpAddress();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return connection.getLocalAddress();
    }

    @Override
    public Buffer getLocalIpAddressAsBuffer() {
        return connection.getLocalIpAddressAsBuffer();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return connection.getRemoteAddress();
    }

    @Override
    public int getRemotePort() {
        return connection.getRemotePort();
    }

    @Override
    public byte[] getRawRemoteIpAddress() {
        return connection.getRawRemoteIpAddress();
    }

    @Override
    public String getRemoteIpAddress() {
        return connection.getRemoteIpAddress();
    }

    @Override
    public Buffer getRemoteIpAddressAsBuffer() {
        return connection.getRemoteIpAddressAsBuffer();
    }

    @Override
    public Transport getTransport() {
        return connection.getTransport();
    }

    @Override
    public void send(final T msg) {
        if (msgToSend == null) {
            msgToSend = msg;
        }
    }

    public void processMessage(final ChannelHandlerContext ctx) {
        if (msgToSend != null) {
            ctx.write(msgToSend);
            msgToSend = null;
        }
    }

    @Override
    public boolean connect() {
        return connection.connect();
    }

    @Override
    public void close() {
        System.err.println("Closing");
    }
}
