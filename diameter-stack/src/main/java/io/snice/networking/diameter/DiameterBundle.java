package io.snice.networking.diameter;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.snice.codecs.codec.diameter.avp.api.HostIpAddress;
import io.snice.codecs.codec.diameter.avp.api.ProductName;
import io.snice.codecs.codec.diameter.avp.type.IpAddress;
import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.diameter.event.DiameterEvent;
import io.snice.networking.diameter.handler.DiameterMessageStreamDecoder2;
import io.snice.networking.diameter.handler.DiameterStreamEncoder;
import io.snice.networking.diameter.peer.PeerTable;
import io.snice.networking.diameter.peer.fsm.PeerContext;
import io.snice.networking.diameter.peer.fsm.PeerData;
import io.snice.networking.diameter.peer.fsm.PeerState;
import io.snice.networking.diameter.peer.impl.DefaultDiameterEnvironment;
import io.snice.networking.diameter.peer.impl.DefaultRoutingEngine;
import io.snice.networking.diameter.yaml.StandardAvpDeserializer;
import io.snice.networking.netty.ProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static io.snice.preconditions.PreConditions.ensureNotNull;

/**
 * Note that this class is accessed in a multi-threaded environment and as such, everything has to
 * be thread safe.
 */
public class DiameterBundle<C extends DiameterAppConfig> implements ProtocolBundle<PeerConnection, DiameterEvent, C> {

    private static final Logger logger = LoggerFactory.getLogger(DiameterBundle.class);

    private final ProtocolHandler encoder;
    private final ProtocolHandler decoder;
    private PeerTable peerTable;
    private C configuration;

    public DiameterBundle() {
        encoder = ProtocolHandler.of("diameter-codec-encoder")
                .withChannelHandler(() -> new DiameterStreamEncoder())
                .withTransport(Transport.tcp)
                .build();

        decoder = ProtocolHandler.of("diameter-codec-decoder")
                .withChannelHandler(() -> new DiameterMessageStreamDecoder2())
                .withTransport(Transport.tcp)
                .build();

    }

    @Override
    public String getBundleName() {
        return "DiameterBundle";
    }

    @Override
    public void initialize(final C config) {
        logger.info("Initializing Diameter Stack");
        ensureNotNull(config, "The configuration object for the \"" + getBundleName() + "\" cannot be null");
        this.configuration = config;
        final var routingEngine = new DefaultRoutingEngine();
        // TODO: perhaps the PeerTable should just be called a DiameterStack instead.
        peerTable = PeerTable.create(configuration.getConfig(), routingEngine);
    }

    @Override
    public CompletionStage<ProtocolBundle<PeerConnection, DiameterEvent, C>> start(final NetworkStack<PeerConnection, DiameterEvent, C> stack) {
        logger.info("Starting Diameter Stack");
        return peerTable.start(stack);
    }

    @Override
    public void stop() {
        logger.info("Stopping Diameter Stack");
    }

    @Override
    public Class<DiameterEvent> getType() {
        return DiameterEvent.class;
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
    public PeerConnection wrapConnection(final Connection<DiameterEvent> connection) {
        // TODO:
        return PeerConnection.of(connection);
    }

    @Override
    public <E extends Environment<PeerConnection, DiameterEvent, C>> E createEnvironment(final NetworkStack<PeerConnection, DiameterEvent, C> stack, final C configuration) {
        return (E) new DefaultDiameterEnvironment(stack, peerTable, configuration);
    }

    @Override
    public Optional<FsmFactory<DiameterEvent, PeerState, PeerContext, PeerData>> getFsmFactory() {
        return Optional.of(peerTable);
    }
}
