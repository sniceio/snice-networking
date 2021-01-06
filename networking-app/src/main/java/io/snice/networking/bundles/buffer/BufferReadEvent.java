package io.snice.networking.bundles.buffer;

import io.snice.buffer.Buffer;
import io.snice.networking.common.ConnectionId;

import static io.snice.buffer.Buffers.assertNotEmpty;
import static io.snice.preconditions.PreConditions.assertNotNull;

public class BufferReadEvent extends BufferEvent {

    public static BufferReadEvent of(final ConnectionId id, final Buffer buffer) {
        assertNotNull(id);
        assertNotEmpty(buffer);
        return new BufferReadEvent(id, buffer);
    }

    @Override
    public BufferReadEvent toReadEvent() {
        return this;
    }

    @Override
    public boolean isReadEvent() {
        return true;
    }

    private BufferReadEvent(final ConnectionId id, final Buffer buffer) {
        super(id, buffer);
    }
}
