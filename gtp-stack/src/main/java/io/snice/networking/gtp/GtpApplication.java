package io.snice.networking.gtp;

import io.snice.networking.app.NetworkApplication;
import io.snice.networking.common.Connection;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.event.GtpEvent;

public abstract class GtpApplication<C extends GtpAppConfig> extends NetworkApplication<GtpEnvironment<C>, Connection<GtpEvent>, GtpEvent, C> {

    public GtpApplication() {
        super(new GtpBundle());
    }
}
