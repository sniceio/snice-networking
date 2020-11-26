package io.snice.networking.gtp;

public interface GtpControlTunnel extends GtpTunnel {

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
