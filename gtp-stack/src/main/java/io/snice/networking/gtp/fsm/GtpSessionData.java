package io.snice.networking.gtp.fsm;

import io.hektor.fsm.Data;
import io.snice.networking.gtp.conf.GtpConfig;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class GtpSessionData implements Data {

    private final GtpConfig config;

    public static GtpSessionData of(final GtpConfig config) {
        assertNotNull(config);
        return new GtpSessionData(config);
    }

    private GtpSessionData(final GtpConfig config) {
        this.config = config;
    }
}
