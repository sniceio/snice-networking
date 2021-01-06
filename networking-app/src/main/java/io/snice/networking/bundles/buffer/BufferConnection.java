package io.snice.networking.bundles.buffer;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.networking.common.Connection;

public interface BufferConnection extends Connection<BufferEvent> {

    void send(Buffer buffer);

    default void send(final String buffer) {
        send(Buffers.wrap(buffer));
    }
}
