package io.snice.networking.gtp.fsm;

import io.hektor.fsm.Data;
import io.snice.networking.gtp.conf.GtpConfig;
import io.snice.preconditions.PreConditions;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class GtpTunnelData implements Data {

    private final GtpConfig config;

    public static GtpTunnelData of(final GtpConfig config) {
        assertNotNull(config);
        return new GtpTunnelData(config);
    }

    private GtpTunnelData(final GtpConfig config) {
        this.config = config;
    }
}
