package io.snice.networking.gtp.impl;

import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;
import io.snice.networking.gtp.GtpControlTunnel;
import io.snice.networking.gtp.GtpEnvironment;
import io.snice.networking.gtp.GtpTunnel;
import io.snice.networking.gtp.GtpUserTunnel;
import io.snice.networking.gtp.PdnSession;
import io.snice.networking.gtp.conf.ControlPlaneConfig;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.conf.UserPlaneConfig;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

public class DefaultGtpEnvironment<C extends GtpAppConfig> implements GtpEnvironment<C> {

    private final InternalGtpStack<C> stack;
    private final UserPlaneConfig userPlaneConfig;
    private final ControlPlaneConfig controlPlaneConfig;

    public DefaultGtpEnvironment(final InternalGtpStack<C> stack) {
        this.stack = stack;
        this.userPlaneConfig = stack.getConfig().getConfig().getUserPlane();
        this.controlPlaneConfig = stack.getConfig().getConfig().getControlPlane();
    }

    @Override
    public C getConfig() {
        return stack.getConfig();
    }

    @Override
    public CompletionStage<GtpControlTunnel> establishControlPlane(final InetSocketAddress remoteAddress) {
        return stack.establishControlPlane(remoteAddress);
    }

    @Override
    public GtpUserTunnel establishUserPlane(final InetSocketAddress remoteAddress) {
        return stack.establishUserPlane(remoteAddress);
    }

    @Override
    public PdnSession.Builder<C> initiateNewPdnSession(final CreateSessionRequest createSessionRequest) {
        return stack.initiateNewPdnSession(createSessionRequest);
    }

    @Override
    public CompletionStage<GtpTunnel> connect(final Transport transport, final InetSocketAddress remoteAddress) throws IllegalTransportException {
        throw new IllegalArgumentException("Please use the other connect methods instead");
    }

}
