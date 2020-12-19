package io.snice.networking.app.buffer;

import io.snice.buffer.Buffer;
import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.app.NetworkApplication;
import io.snice.networking.bundles.buffer.BufferBundle;
import io.snice.networking.bundles.buffer.BufferConnection;
import io.snice.networking.bundles.buffer.BufferEvent;

/**
 * Simple application that only deals with {@link Buffer}sl
 */
public abstract class BufferApplication<C extends NetworkAppConfig> extends NetworkApplication<BufferEnvironment<C>, BufferConnection, BufferEvent, C> {

    public BufferApplication() {
        super(new BufferBundle<>());
    }
}
