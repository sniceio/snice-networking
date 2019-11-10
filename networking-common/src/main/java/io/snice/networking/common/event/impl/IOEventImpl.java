package io.snice.networking.common.event.impl;

import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.event.IOEvent;

/**
 * @author jonas@jonasborjesson.com
 */
public class IOEventImpl<T> implements IOEvent<T> {

    private final ChannelContext<T> ctx;
    private final long arrivalTime;

    public IOEventImpl(final ChannelContext<T> channelContext, final long arrivalTime) {
        this.ctx = channelContext;
        this.arrivalTime = arrivalTime;
    }


    @Deprecated
    @Override
    public Connection<T> connection() {
        throw new RuntimeException("Deprecated");
    }

    @Override
    public ChannelContext<T> channelContext() {
        return ctx;
    }

    @Override
    public long arrivalTime() {
        return arrivalTime;
    }
}
