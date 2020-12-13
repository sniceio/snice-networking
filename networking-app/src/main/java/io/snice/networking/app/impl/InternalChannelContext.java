package io.snice.networking.app.impl;

import io.snice.networking.app.ConnectionContext;
import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.Connection;

public interface InternalChannelContext<T> extends ChannelContext<T> {

    ConnectionContext<Connection<T>, T> getConnectionContext();

}
