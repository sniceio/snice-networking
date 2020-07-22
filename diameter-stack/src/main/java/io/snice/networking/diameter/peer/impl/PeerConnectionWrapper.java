package io.snice.networking.diameter.peer.impl;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.avp.api.OriginHost;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.diameter.Peer;
import io.snice.networking.diameter.peer.PeerConfiguration;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;
import static io.snice.preconditions.PreConditions.ensureNotNull;

public class PeerConnectionWrapper implements Peer {
    private final Connection<DiameterMessage> actualConnection;

    public static Peer of(final Connection<DiameterMessage> actualConnection) {
        assertNotNull(actualConnection, "The underlying connection cannot be null");
        return new PeerConnectionWrapper(actualConnection);
    }

    @Override
    public OriginHost getOriginHost() {
        return null;
    }

    @Override
    public PeerConfiguration getConfiguration() {
        return null;
    }

    @Override
    public MODE getMode() {
        return null;
    }

    @Override
    public void send(final DiameterMessage.Builder msg) {
        ensureNotNull(msg, "You cannot send a null message");
        // TODO: check if we are to add the origin host and realm
        // to this message
        send(msg.build());
    }

    private PeerConnectionWrapper(final Connection<DiameterMessage> actualConnection) {
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
    public void send(final DiameterMessage msg) {
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