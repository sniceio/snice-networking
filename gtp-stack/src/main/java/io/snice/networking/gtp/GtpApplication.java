package io.snice.networking.gtp;

import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.impl.DefaultGtpStack;

public abstract class GtpApplication<C extends GtpAppConfig> extends NetworkApplication<GtpEnvironment<C>, GtpTunnel, GtpEvent, C> {

    public GtpApplication() {
        super(new DefaultGtpStack());
    }

    /**
     * In order to be in full control, the {@link GtpApplication} will not allow the application built on top
     * of it to use this method directly but the application rather has to override
     * {@link #initialize(GtpBootstrap)}. This so that the {@link GtpApplication} can insert itself and
     * allow the application to use specific callbacks per
     *
     * @param bootstrap
     */
    @Override
    public final void initialize(final NetworkBootstrap<GtpTunnel, GtpEvent, C> bootstrap) {
        final var stack = (DefaultGtpStack) super.getProtocolBundle();
        stack.initializeApplication(this, bootstrap);
    }

    @Override
    protected ProtocolBundle<GtpTunnel, GtpEvent, C> getProtocolBundle() {
        throw new IllegalArgumentException("Sub-classes are not allowed to obtain the Protocol Bundle");
    }

    /**
     * GTP applications need to override this initialize method, which has the same purpose as the
     * general {@link NetworkApplication#initialize(NetworkBootstrap)} but is tailored to GTP only.
     */
    public abstract void initialize(final GtpBootstrap<C> bootstrap);
}
