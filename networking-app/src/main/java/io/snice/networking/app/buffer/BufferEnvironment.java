package io.snice.networking.app.buffer;

import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.bundles.buffer.BufferConnection;
import io.snice.networking.bundles.buffer.BufferEvent;

public interface BufferEnvironment<C extends NetworkAppConfig> extends Environment<BufferConnection, BufferEvent, C> {
}
