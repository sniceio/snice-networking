package io.snice.networking.bundles.buffer;

import io.snice.buffer.Buffer;
import io.snice.networking.common.ConnectionId;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class BufferWriteEvent extends BufferEvent {

    public static BufferWriteEvent of(final ConnectionId id, final Buffer buffer) {
        assertNotNull(id);
        assertNotNull(buffer);
        return new BufferWriteEvent(id, buffer);
    }

    @Override
    public BufferWriteEvent toWriteEvent() {
        return this;
    }

    @Override
    public boolean isWriteEvent() {
        return true;
    }

    private BufferWriteEvent(final ConnectionId id, final Buffer buffer) {
        super(id, buffer);
    }
}
