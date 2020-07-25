package io.snice.networking.diameter.peer;

import io.snice.networking.diameter.PeerConnection;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * The <code>*Configuration /code> classes, such as {@link PeerConfiguration}, are part of the public API
 * whereas the <code>*Settings</code> are for internal implementation use only. The main reason is simply
 * there are settings for e.g. a {@link PeerConnection} that shouldn't be exposed to the user and anything in the
 * {@link PeerConfiguration} is and also may then be available for configuration in the config files.
 */
public class PeerSettings {

    private final PeerConfiguration config;

    public static PeerSettings from(final PeerConfiguration config) {
        assertNotNull(config);
        return new PeerSettings(config);
    }

    private PeerSettings(final PeerConfiguration config) {
        this.config = config;
    }

}
