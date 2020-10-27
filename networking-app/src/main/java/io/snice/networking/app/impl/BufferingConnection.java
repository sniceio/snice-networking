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

    private final ConnectionId connectionId;

    private T msgToSend;

    public BufferingConnection(final ConnectionId connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public ConnectionId id() {
        return connectionId;
    }

    @Override
    public Optional<URI> getVipAddress() {
        throw new RuntimeException("reworking things");
    }

    @Override
    public int getLocalPort() {
        return connectionId.getLocalPort();
    }

    @Override
    public int getDefaultPort() {
        throw new RuntimeException("reworking things");
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
        throw new RuntimeException("reworking things");
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
        throw new RuntimeException("reworking things");
    }

    @Override
    public Transport getTransport() {
        return connectionId.getProtocol();
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
        return true;
    }

    @Override
    public void close() {
        System.err.println("Closing");
    }
}
