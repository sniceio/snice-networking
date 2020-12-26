package io.snice.networking.examples.gtp;

import io.snice.networking.gtp.GtpEnvironment;

public class FourthGenerationDevice implements Device {
    private final GtpEnvironment<GtpConfig> environment;

    private FourthGenerationDevice(final GtpEnvironment<GtpConfig> environment) {
        this.environment = environment;
    }
}
