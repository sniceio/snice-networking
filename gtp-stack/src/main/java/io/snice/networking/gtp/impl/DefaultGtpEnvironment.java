package io.snice.networking.gtp.impl;

import io.snice.networking.app.NetworkStack;
import io.snice.networking.common.Connection;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;
import io.snice.networking.gtp.GtpControlTunnel;
import io.snice.networking.gtp.GtpEnvironment;
import io.snice.networking.gtp.GtpTunnel;
import io.snice.networking.gtp.GtpUserTunnel;
import io.snice.networking.gtp.conf.ControlPlaneConfig;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.conf.UserPlaneConfig;
import io.snice.networking.gtp.event.GtpEvent;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultGtpEnvironment<C extends GtpAppConfig> implements GtpEnvironment<C> {

    private final NetworkStack<Connection<GtpEvent>, GtpEvent, C> stack;
    private final C config;
    private final UserPlaneConfig userPlaneConfig;
    private final ControlPlaneConfig controlPlaneConfig;

    public DefaultGtpEnvironment(final NetworkStack<Connection<GtpEvent>, GtpEvent, C> stack, final C config) {
        this.stack = stack;
        this.config = config;
        this.userPlaneConfig = config.getConfig().getUserPlane();
        this.controlPlaneConfig = config.getConfig().getControlPlane();
    }

    @Override
    public C getConfig() {
        return config;
    }

    @Override
    public CompletionStage<GtpControlTunnel> establishControlPlane(final InetSocketAddress remoteAddress) {
        if (!controlPlaneConfig.isEnable()) {
            throw new IllegalArgumentException("The Control Plane is not configured for this application");
        }

        assertNotNull(remoteAddress, "The remote address cannot be null");
        final var nic = stack.getNetworkInterface(controlPlaneConfig.getNic()).orElseThrow(() ->
                new IllegalArgumentException("Unable to find the Network Interface to use for the Control Plane. " +
                        "The configuration says to use \"" + controlPlaneConfig.getNic() + "\" but no such" +
                        "interface exists"));
        return nic.connect(Transport.udp, remoteAddress).thenApply(GtpControlTunnel::of);
    }

    @Override
    public CompletionStage<GtpUserTunnel> establishUserPlane(final InetSocketAddress remoteAddress) {
        if (!userPlaneConfig.isEnable()) {
            throw new IllegalArgumentException("The User Plane is not configured for this application");
        }
        assertNotNull(remoteAddress, "The remote address cannot be null");
        final var nic = stack.getNetworkInterface(userPlaneConfig.getNic()).orElseThrow(() ->
                new IllegalArgumentException("Unable to find the Network Interface to use for the User Plane. " +
                        "The configuration says to use \"" + userPlaneConfig.getNic() + "\" but no such" +
                        "interface exists"));
        return nic.connect(Transport.udp, remoteAddress).thenApply(GtpUserTunnel::of);
    }

    @Override
    public CompletionStage<GtpTunnel> connect(final Transport transport, final InetSocketAddress remoteAddress) throws IllegalTransportException {
        throw new IllegalArgumentException("Please use the other connect methods instead");
    }

}
