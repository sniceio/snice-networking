package io.snice.networking.gtp;

import io.snice.networking.app.Environment;
import io.snice.networking.common.Connection;
import io.snice.networking.gtp.event.GtpEvent;

public interface GtpEnvironment<C extends GtpAppConfig> extends Environment<Connection<GtpEvent>, GtpEvent, C> {

    @Override
    C getConfig();

}
