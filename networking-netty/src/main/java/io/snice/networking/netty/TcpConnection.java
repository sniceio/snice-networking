/**
 *
 */
package io.snice.networking.netty;

import io.netty.channel.Channel;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;

/**
 * @author jonas@jonasborjesson.com
 */
public final class TcpConnection extends AbstractConnection {


    public TcpConnection(final Channel channel, final ConnectionId id, final Optional<URI> vipAddress) {
        super(channel, id, vipAddress);
    }

    public TcpConnection(final Channel channel, final InetSocketAddress remote, final Optional<URI> vipAddress) {
        super(Transport.tcp, channel, remote, vipAddress);
    }

    public TcpConnection(final Channel channel, final InetSocketAddress remote) {
        super(Transport.tcp, channel, remote, null);
    }

    @Override
    public boolean isTCP() {
        return true;
    }

    @Override
    public int getDefaultPort() {
        return 5060;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(final Object msg) {
        // final var byteBuf = toByteBuf((Buffer)msg);
        // write(byteBuf);
        write(msg);
    }

    @Override
    public boolean connect() {
        return true;
    }

}
