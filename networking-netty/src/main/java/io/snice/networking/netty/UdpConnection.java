package io.snice.networking.netty;

import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import io.snice.buffer.Buffer;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;

/**
 * Encapsulates a
 * 
 * @author jonas@jonasborjesson.com
 */
public final class UdpConnection extends AbstractConnection {

    public UdpConnection(final Channel channel, final ConnectionId id, final Optional<URI> vipAddress) {
        super(channel, id, vipAddress);
    }

    public UdpConnection(final Channel channel, final InetSocketAddress remoteAddress, final Optional<URI> vipAddress) {
        super(Transport.udp, channel, remoteAddress, vipAddress);
    }

    public UdpConnection(final Channel channel, final InetSocketAddress remoteAddress) {
        super(Transport.udp, channel, remoteAddress, Optional.empty());
    }

    @Override
    public int getDefaultPort() {
        return 5060;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUDP() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(final Object msg) {
        final var byteBuf = toByteBuf((Buffer)msg);
        final var pkt = new DatagramPacket(byteBuf, getRemoteAddress());
        System.err.println("Udp socket is sending " + msg);
        write(pkt);
    }

    @Override
    public boolean connect() {
        return true;
    }


}
