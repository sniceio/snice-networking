package io.snice.networking.gtp;

import com.fasterxml.jackson.databind.Module;
import io.hektor.fsm.Data;
import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.common.fsm.NetworkContext;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.impl.DefaultGtpEnvironment;
import io.snice.networking.netty.ProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static io.snice.preconditions.PreConditions.ensureNotNull;

public class GtpBundle<C extends GtpAppConfig> implements ProtocolBundle<Connection<GtpEvent>, GtpEvent, C> {

    private static final Logger logger = LoggerFactory.getLogger(GtpBundle.class);

    private final List<ProtocolHandler> encoders;
    private final List<ProtocolHandler> decoders;

    private C configuration;

    public GtpBundle() {
        encoders = List.of();
        decoders = List.of();
    }

    @Override
    public void initialize(final C config) {
        logger.info("Initializing GTP Stack");
        ensureNotNull(config, "The configuration object for the \"" + getBundleName() + "\" cannot be null");
        this.configuration = config;
    }

    @Override
    public String getBundleName() {
        return "GtpBundle";
    }

    @Override
    public Class<GtpEvent> getType() {
        return GtpEvent.class;
    }

    @Override
    public CompletionStage<ProtocolBundle<Connection<GtpEvent>, GtpEvent, C>> start(final NetworkStack<Connection<GtpEvent>, GtpEvent, C> stack) {
        logger.info("Starting GTP Stack");
        return CompletableFuture.completedFuture(this);
    }

    @Override
    public void stop() {
        logger.info("Stopping GTP Stack");
    }

    @Override
    public <E extends Environment<Connection<GtpEvent>, GtpEvent, C>> E createEnvironment(final NetworkStack<Connection<GtpEvent>, GtpEvent, C> stack, final C configuration) {
        return (E) new DefaultGtpEnvironment(stack, configuration);
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
    public Connection<GtpEvent> wrapConnection(final Connection<GtpEvent> connection) {
        return connection;
    }

    @Override
    public <S extends Enum<S>, C1 extends NetworkContext<GtpEvent>, D extends Data> Optional<FsmFactory<GtpEvent, S, C1, D>> getFsmFactory() {
        return Optional.empty();
    }
}
