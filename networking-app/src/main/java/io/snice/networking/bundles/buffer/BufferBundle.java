package io.snice.networking.bundles.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.app.buffer.BufferEnvironment;
import io.snice.networking.app.impl.DefaultEnvironment;
import io.snice.networking.app.impl.UdpReadEvent;
import io.snice.networking.bundles.BundleSupport;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.ConnectionSupport;
import io.snice.networking.common.Transport;
import io.snice.networking.netty.ProtocolHandler;
import io.snice.time.Clock;
import io.snice.time.SystemClock;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * A simple {@link ProtocolBundle} for applications (probably only simple test apps) that only
 * deal with sending/receiving Buffers over a network.
 */
public class BufferBundle<C extends NetworkAppConfig> extends BundleSupport<BufferConnection, BufferEvent, C> {

    private final List<ProtocolHandler> encoders;
    private final List<ProtocolHandler> decoders;

    public BufferBundle() {
        super(BufferEvent.class);
        final var bufferEncoder = ProtocolHandler.of("Buffer-encoder")
                .withChannelHandler(() -> new BufferDatagramEncoder())
                .withTransports(Transport.udp)
                .build();

        final var bufferDecoder = ProtocolHandler.of("Buffer-decoder")
                .withChannelHandler(() -> new BufferDatagramDecoder())
                .withTransports(Transport.udp)
                .build();

        encoders = List.of(bufferEncoder);
        decoders = List.of(bufferDecoder);
    }

    @Override
    public BufferConnection wrapConnection(final Connection<BufferEvent> connection) {
        return new DefaultBufferConnection(connection);
    }

    @Override
    public String getBundleName() {
        return "BufferBundle";
    }

    @Override
    public <E extends Environment<BufferConnection, BufferEvent, C>> E createEnvironment(final NetworkStack<BufferConnection, BufferEvent, C> stack, final C configuration) {
        return (E) new DefaultBufferEnvironment(stack, configuration);
    }

    @Override
    public List<ProtocolHandler> getProtocolEncoders() {
        return encoders;
    }

    @Override
    public List<ProtocolHandler> getProtocolDecoders() {
        return decoders;
    }

    @ChannelHandler.Sharable
    private static class BufferDatagramDecoder extends MessageToMessageDecoder<DatagramPacket> {
        private final Clock clock;

        private BufferDatagramDecoder() {
            this.clock = new SystemClock();
        }

        @Override
        protected void decode(final ChannelHandlerContext ctx, final DatagramPacket udp, final List<Object> list) throws Exception {
            final long arrivalTime = clock.getCurrentTimeMillis();
            final var content = udp.content();

            final byte[] b = new byte[content.readableBytes()];
            content.getBytes(0, b);
            final var buffer = Buffers.wrap(b);
            final var id = ConnectionId.create(Transport.udp, (InetSocketAddress) ctx.channel().localAddress(), udp.sender());
            final var evt = BufferReadEvent.of(id, buffer);

            list.add(UdpReadEvent.create(ctx, udp, evt, arrivalTime));
        }
    }

    private static class BufferDatagramEncoder extends MessageToMessageEncoder<BufferWriteEvent> {

        private BufferDatagramEncoder() {
        }

        @Override
        protected void encode(final ChannelHandlerContext ctx, final BufferWriteEvent event, final List<Object> out) {
            final var pkt = new DatagramPacket(toByteBuf(ctx.channel(), event), event.getConnectionId().getRemoteAddress());
            out.add(pkt);
        }

        public static ByteBuf toByteBuf(final Channel channel, final BufferWriteEvent evt) {
            final var buffer = evt.getBuffer();
            final int capacity = buffer.capacity();
            final ByteBuf byteBuf = channel.alloc().buffer(capacity, capacity);
            for (int i = 0; i < buffer.capacity(); ++i) {
                byteBuf.writeByte(buffer.getByte(i));
            }
            return byteBuf;
        }

    }

    private static class DefaultBufferConnection extends ConnectionSupport<BufferEvent> implements BufferConnection {

        protected DefaultBufferConnection(final Connection<BufferEvent> actualConnection) {
            super(actualConnection);
        }

        @Override
        public void send(final Buffer buffer) {
            actualConnection.send(BufferWriteEvent.of(id(), buffer));
        }

        @Override
        public void send(final BufferEvent msg) {
            actualConnection.send(msg);
        }
    }

    private static class DefaultBufferEnvironment<C extends NetworkAppConfig> extends DefaultEnvironment<BufferConnection, BufferEvent, C> implements BufferEnvironment<C> {
        private DefaultBufferEnvironment(final NetworkStack<BufferConnection, BufferEvent, C> stack, final C config) {
            super(stack, config);
        }
    }
}
