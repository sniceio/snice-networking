package io.snice.networking.bundles;

import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;
import io.snice.networking.netty.ProtocolHandler;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A simple {@link ProtocolBundle} for applications (probably only simple test apps) that only
 * deal with sending/receiving Strings over a network.
 */
public class StringBundle extends BundleSupport<Connection<String>, String> {

    private final List<ProtocolHandler> encoders;
    private final List<ProtocolHandler> decoders;

    public StringBundle() {
        super(String.class);
        final var encoder = ProtocolHandler.of("String-encoder")
                .withChannelHandler(() -> new StringEncoder(StandardCharsets.UTF_8))
                .withTransports(Transport.tcp, Transport.udp)
                .build();
        encoders = List.of(encoder);

        final var frameDecoder = ProtocolHandler.of("String-frame-decoder")
                .withChannelHandler(() -> new LineBasedFrameDecoder(80))
                .withTransports(Transport.tcp, Transport.udp)
                .build();

        final var stringDecoder = ProtocolHandler.of("String-decoder")
                .withChannelHandler(() -> new StringDecoder(StandardCharsets.UTF_8))
                .withTransports(Transport.tcp, Transport.udp)
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
}
