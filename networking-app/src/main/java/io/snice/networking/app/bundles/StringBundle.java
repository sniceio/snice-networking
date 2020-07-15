package io.snice.networking.app.bundles;

import com.fasterxml.jackson.databind.Module;
import io.hektor.fsm.Data;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.snice.networking.app.AppBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.common.fsm.NetworkContext;
import io.snice.networking.netty.ProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * A simple {@link AppBundle} for applications (probably only simple test apps) that only
 * deal with sending/receiving Strings over a network.
 */
public class StringBundle implements AppBundle<Connection<String>, String> {

    private static final Logger logger = LoggerFactory.getLogger(StringBundle.class);

    private final List<ProtocolHandler> encoders;
    private final List<ProtocolHandler> decoders;

    public StringBundle() {
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
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public Optional<Module> getObjectMapModule() {
        return Optional.empty();
    }

    @Override
    public List<ProtocolHandler> getProtocolEncoders() {
        return encoders;
    }

    @Override
    public List<ProtocolHandler> getProtocolDecoders() {
        return decoders;
    }

    @Override
    public Connection<String> wrapConnection(final Connection<String> connection) {
        return connection;
    }

    @Override
    public <S extends Enum<S>, C extends NetworkContext<String>, D extends Data> Optional<FsmFactory<String, S, C, D>> getFsmFactory() {
        return Optional.empty();
    }
}
