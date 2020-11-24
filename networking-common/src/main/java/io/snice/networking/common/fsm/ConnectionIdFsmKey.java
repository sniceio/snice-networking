package io.snice.networking.common.fsm;

import io.snice.networking.common.ConnectionId;

import java.util.Objects;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * Basic implementation for those FSMs that only base their
 * key off of the {@link ConnectionId}
 */
public class ConnectionIdFsmKey implements FsmKey {

    private final ConnectionId id;

    public static ConnectionIdFsmKey of(final ConnectionId id) {
        assertNotNull(id, "The connection id cannot be null");
        return new ConnectionIdFsmKey(id);
    }

    private ConnectionIdFsmKey(final ConnectionId id) {
        this.id = id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ConnectionIdFsmKey that = (ConnectionIdFsmKey) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
