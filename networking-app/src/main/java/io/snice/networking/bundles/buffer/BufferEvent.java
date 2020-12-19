package io.snice.networking.bundles.buffer;

import io.snice.buffer.Buffer;
import io.snice.networking.common.ConnectionId;

public abstract class BufferEvent {

    private final ConnectionId id;
    private final Buffer buffer;

    protected BufferEvent(final ConnectionId id, final Buffer buffer) {
        this.id = id;
        this.buffer = buffer;
    }

    public ConnectionId getConnectionId() {
        return id;
    }

    public BufferReadEvent toReadEvent() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + BufferReadEvent.class.getName());
    }

    public boolean isReadEvent() {
        return false;
    }

    public BufferWriteEvent toWriteEvent() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + BufferWriteEvent.class.getName());
    }

    public boolean isWriteEvent() {
        return false;
    }

    public Buffer getBuffer() {
        return buffer;
    }

}
