package io.snice.networking.gtp;

import io.snice.networking.app.ConnectionContext;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.event.GtpEvent;

import java.util.function.Predicate;

public interface GtpBootstrap<C extends GtpAppConfig> {

    ConnectionContext.Builder<GtpTunnel, GtpEvent, GtpEvent> onConnection(Predicate<ConnectionId> condition);
}
