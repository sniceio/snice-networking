/**
 * 
 */
package io.snice.networking.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Optional;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class AbstractConnection<T> implements Connection<T> {

    private final ConnectionId id;
    private final Channel channel;
    private final InetSocketAddress remote;
    private final Optional<URI> vipAddress;
    private final static AttributeKey<Object> key = AttributeKey.newInstance("generic_object");

    /*
     * protected AbstractConnection(final ChannelHandlerContext ctx, final InetSocketAddress remote)
     * { this.ctx = ctx; this.channel = null; this.remote = remote; }
     */

    protected AbstractConnection(final Transport transport,
                                 final Channel channel,
                                 final InetSocketAddress remote,
                                 final Optional<URI> vipAddress) {
        this(channel, ConnectionId.create(transport, (InetSocketAddress)channel.localAddress(), remote), vipAddress);
    }

    protected AbstractConnection(final Channel channel,
                                 final ConnectionId id,
                                 final Optional<URI> vipAddress) {
        this.id = id;
        this.channel = channel;
        this.remote = id.getRemoteAddress();
        this.vipAddress = vipAddress == null ? Optional.empty() : vipAddress;
    }

    protected AbstractConnection(final Transport transport, final Channel channel, final InetSocketAddress remote) {
        this(transport, channel, remote, null);
    }

    @Override
    public Optional<URI> getVipAddress() {
        return vipAddress;
    }

    protected Channel channel() {
        return this.channel;
    }

    @Override
    public Transport getTransport() {
        return this.id.getProtocol();
    }

    @Override
    public ConnectionId id() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.id.toString();
    }

    @Override
    public void close() {
        // TODO: do we need to do more?
        channel.close();
    }
    @Override
    public byte[] getRawRemoteIpAddress() {
        return this.remote.getAddress().getAddress();
    }

    @Override
    public byte[] getRawLocalIpAddress() {
        final SocketAddress local = this.channel.localAddress();
        final InetAddress address = ((InetSocketAddress) local).getAddress();
        return address.getAddress();
    }

    @Override
    public final InetSocketAddress getLocalAddress() {
        return (InetSocketAddress)this.channel.localAddress();
    }

    @Override
    public final String getLocalIpAddress() {
        final SocketAddress local = this.channel.localAddress();
        return ((InetSocketAddress) local).getAddress().getHostAddress();
    }

    @Override
    public final Buffer getLocalIpAddressAsBuffer() {
        return Buffers.wrap(getLocalIpAddress());
    }

    @Override
    public final InetSocketAddress getRemoteAddress() {
        return this.remote;
    }

    @Override
    public final String getRemoteIpAddress() {
        return this.remote.getAddress().getHostAddress();
    }

    @Override
    public final Buffer getRemoteIpAddressAsBuffer() {
        return Buffers.wrap(getRemoteIpAddress());
    }

    @Override
    public int getLocalPort() {
        final SocketAddress local = this.channel.localAddress();
        return ((InetSocketAddress) local).getPort();
    }

    @Override
    public int getRemotePort() {
        return this.remote.getPort();
    }

    protected void write(final Object o) {
        channel.write(o, channel.voidPromise());
        channel.flush();
    }

    protected ByteBuf toByteBuf(final Buffer msg) {
        final int capacity = msg.capacity();
        final ByteBuf buffer = channel.alloc().buffer(capacity, capacity);
        for (int i = 0; i < msg.capacity(); ++i) {
            buffer.writeByte(msg.getByte(i));
        }
        return buffer;
    }
}
