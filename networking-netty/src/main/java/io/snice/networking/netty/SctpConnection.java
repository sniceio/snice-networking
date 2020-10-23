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
public final class SctpConnection<T> extends AbstractConnection<T> {


    public SctpConnection(final Channel channel, final ConnectionId id, final Optional<URI> vipAddress) {
        super(channel, id, vipAddress);
    }

    public SctpConnection(final Channel channel, final InetSocketAddress remote, final Optional<URI> vipAddress) {
        super(Transport.sctp, channel, remote, vipAddress);
    }

    public SctpConnection(final Channel channel, final InetSocketAddress remote) {
        super(Transport.sctp, channel, remote, null);
    }

    @Override
    public boolean isSCTP() {
        return true;
    }

    @Override
    public int getDefaultPort() {
        return 3868;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(final T msg) {
        write(msg);
    }

    @Override
    public boolean connect() {
        return true;
    }

}
