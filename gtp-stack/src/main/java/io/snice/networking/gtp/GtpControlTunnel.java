package io.snice.networking.gtp;

import io.snice.networking.common.Connection;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.impl.DefaultGtpControlTunnel;

public interface GtpControlTunnel extends GtpTunnel {

    static GtpControlTunnel of(final Connection<GtpEvent> actualConnection) {
        return DefaultGtpControlTunnel.of(actualConnection);
    }

    PdnSession.Builder createPdnSession(String imsi);

    @Override
    default boolean isControlTunnel() {
        return true;
    }

    @Override
    default GtpControlTunnel toControlTunnel() {
        return this;
    }
}
