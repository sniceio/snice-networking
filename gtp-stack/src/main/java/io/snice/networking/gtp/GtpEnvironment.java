package io.snice.networking.gtp;

import io.snice.networking.app.Environment;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.preconditions.PreConditions;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

import static io.snice.preconditions.PreConditions.assertNotEmpty;

public interface GtpEnvironment<C extends GtpAppConfig> extends Environment<GtpTunnel, GtpEvent, C> {

    @Override
    C getConfig();

    CompletionStage<GtpControlTunnel> establishControlPlane(final InetSocketAddress remoteAddress);

    default CompletionStage<GtpControlTunnel> establishControlPlane(final String remoteHost, final int remoteIp ) {
        assertNotEmpty(remoteHost, "The remote host cannot be null or the empty string");
        return establishControlPlane(new InetSocketAddress(remoteHost, remoteIp));
    }

    CompletionStage<GtpUserTunnel> establishUserPlane(final InetSocketAddress remoteAddress);

    default CompletionStage<GtpUserTunnel> establishUserPlane(final String remoteHost, final int remoteIp ) {
        assertNotEmpty(remoteHost, "The remote host cannot be null or the empty string");
        return establishUserPlane(new InetSocketAddress(remoteHost, remoteIp));
    }

}
