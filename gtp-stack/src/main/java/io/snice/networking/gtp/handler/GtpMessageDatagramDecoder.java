package io.snice.networking.gtp.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.app.impl.UdpReadEvent;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageReadEvent;
import io.snice.networking.netty.UdpConnection;
import io.snice.time.Clock;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public class GtpMessageDatagramDecoder extends MessageToMessageDecoder<DatagramPacket> {

    private final Optional<URI> vipAddress = Optional.empty();
    private final Clock clock;

    public GtpMessageDatagramDecoder(final Clock clock) {
        this.clock = clock;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final DatagramPacket udp, final List<Object> list) throws Exception {
        final long arrivalTime = clock.getCurrentTimeMillis();
        final var content = udp.content();

        final byte[] b = new byte[content.readableBytes()];
        content.getBytes(0, b);

        final var buffer = Buffers.wrap(b);
        final var msg = GtpMessage.frame(buffer);
        final var connection = new UdpConnection<GtpEvent>(ctx.channel(), udp.sender(), vipAddress);
        final var evt = GtpMessageReadEvent.of(msg, connection);
        list.add(UdpReadEvent.create(ctx, udp, evt, arrivalTime));
    }
}
