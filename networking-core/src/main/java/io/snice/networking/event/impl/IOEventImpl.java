package io.snice.networking.event.impl;

import io.snice.networking.common.Connection;
import io.snice.networking.event.IOEvent;

/**
 * @author jonas@jonasborjesson.com
 */
public class IOEventImpl implements IOEvent {

    private final Connection connection;
    private final long arrivalTime;

    public IOEventImpl(final Connection connection, final long arrivalTime) {
        this.connection = connection;
        this.arrivalTime = arrivalTime;
    }

    @Override
    public final Connection connection() {
        return connection;
    }

    @Override
    public long arrivalTime() {
        return arrivalTime;
    }
}
