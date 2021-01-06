package io.snice.networking.bundles;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.string.StringEncoder;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.app.impl.UdpReadEvent;
import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;
import io.snice.networking.netty.ProtocolHandler;
import io.snice.time.Clock;
import io.snice.time.SystemClock;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A simple {@link ProtocolBundle} for applications (probably only simple test apps) that only
 * deal with sending/receiving Strings over a network.
 */
public class StringBundle<C extends NetworkAppConfig> extends BundleSupport<Connection<String>, String, C> {

    private final List<ProtocolHandler> encoders;
    private final List<ProtocolHandler> decoders;

    public StringBundle() {
        super(String.class);
        final var encoder = ProtocolHandler.of("String-encoder")
                .withChannelHandler(() -> new StringDatagramEncoder(StandardCharsets.UTF_8))
                .withTransports(Transport.udp)
                .build();

        final var encoderTcp = ProtocolHandler.of("String-encoder")
                .withChannelHandler(() -> new StringEncoder(StandardCharsets.UTF_8))
                .withTransports(Transport.tcp)
                .build();
        encoders = List.of(encoder, encoderTcp);

        final var frameDecoder = ProtocolHandler.of("String-frame-decoder")
                .withChannelHandler(() -> new LineBasedFrameDecoder(80))
                .withTransports(Transport.tcp)
                .build();

        final var stringDecoder = ProtocolHandler.of("String-decoder")
                .withChannelHandler(() -> new StringDatagramDecoder(StandardCharsets.UTF_8))
                .withTransports(Transport.udp)
                .build();

        decoders = List.of(frameDecoder, stringDecoder);
    }

    @Override
    public String getBundleName() {
        return "StringBundle";
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
    private static class StringDatagramDecoder extends MessageToMessageDecoder<DatagramPacket> {
        private final Charset charset;
        private final Clock clock;

        private StringDatagramDecoder(final Charset charset) {
            this.charset = charset;
            this.clock = new SystemClock();
        }

        @Override
        protected void decode(final ChannelHandlerContext ctx, final DatagramPacket udp, final List<Object> list) throws Exception {
            final long arrivalTime = clock.getCurrentTimeMillis();
            final var content = udp.content();

            final byte[] b = new byte[content.readableBytes()];
            content.getBytes(0, b);

            final var str = new String(b, charset);
            list.add(UdpReadEvent.create(ctx, udp, str, arrivalTime));
        }
    }

    private static class StringDatagramEncoder extends MessageToMessageEncoder<String> {

        private final Charset charset;

        private StringDatagramEncoder(final Charset charset) {
            this.charset = charset;
        }

        @Override
        protected void encode(final ChannelHandlerContext ctx, final String string, final List<Object> out) {
            System.err.println("============= encoding, not sure I'm getting here");
            final var byteBuf = ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(string), this.charset);
            // final DatagramPacket pkt = new DatagramPacket(byteBuf, write.getConnectionId().getRemoteAddress());
            // out.add(pkt);
        }

    }
}
