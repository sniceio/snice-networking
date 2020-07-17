package io.snice.networking.diameter;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.avp.api.HostIpAddress;
import io.snice.codecs.codec.diameter.avp.api.ProductName;
import io.snice.codecs.codec.diameter.avp.type.IpAddress;
import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.diameter.handler.DiameterMessageStreamDecoder2;
import io.snice.networking.diameter.handler.DiameterStreamEncoder;
import io.snice.networking.diameter.peer.PeerContext;
import io.snice.networking.diameter.peer.PeerData;
import io.snice.networking.diameter.peer.PeerFactory;
import io.snice.networking.diameter.peer.PeerState;
import io.snice.networking.diameter.peer.impl.DefaultPeerFactory;
import io.snice.networking.diameter.yaml.StandardAvpDeserializer;
import io.snice.networking.netty.ProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class DiameterBundle<C extends DiameterAppConfig> implements ProtocolBundle<Peer, DiameterMessage, C> {

    private static final Logger logger = LoggerFactory.getLogger(DiameterBundle.class);

    private final ProtocolHandler encoder;
    private final ProtocolHandler decoder;
    private final PeerFactory peerFactory;

    public DiameterBundle() {
        encoder = ProtocolHandler.of("diameter-codec-encoder")
                .withChannelHandler(() -> new DiameterStreamEncoder())
                .withTransport(Transport.tcp)
                .build();

        decoder = ProtocolHandler.of("diameter-codec-decoder")
                .withChannelHandler(() -> new DiameterMessageStreamDecoder2())
                .withTransport(Transport.tcp)
                .build();

        peerFactory = new DefaultPeerFactory(null);
    }

    @Override
    public String getBundleName() {
        return "DiameterBundle";
    }

    @Override
    public void start() {
        logger.info("Starting diameter stack");
    }

    @Override
    public void stop() {
        logger.info("Stopping diameter stack");
    }

    @Override
    public Class<DiameterMessage> getType() {
        return DiameterMessage.class;
    }

    @Override
    public Optional<Module> getObjectMapModule() {
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(ProductName.class, new StandardAvpDeserializer<>(ProductName::of));
        module.addDeserializer(HostIpAddress.class, new StandardAvpDeserializer<>(value -> HostIpAddress.of(IpAddress.createIpv4Address(value))));
        return Optional.of(module);
    }

    @Override
    public List<ProtocolHandler> getProtocolEncoders() {
        return List.of(encoder);
    }

    @Override
    public List<ProtocolHandler> getProtocolDecoders() {
        return List.of(decoder);
    }

    @Override
    public Peer wrapConnection(final Connection<DiameterMessage> connection) {
        return Peer.of(connection);
    }

    @Override
    public <E extends Environment<Peer, DiameterMessage, C>> E createEnvironment(final NetworkStack<Peer, DiameterMessage, C> stack, final C configuration) {
        return (E) new DiameterEnvironment(stack, configuration);
    }

    @Override
    public Optional<FsmFactory<DiameterMessage, PeerState, PeerContext, PeerData>> getFsmFactory() {
        return Optional.of(peerFactory);
    }
}
