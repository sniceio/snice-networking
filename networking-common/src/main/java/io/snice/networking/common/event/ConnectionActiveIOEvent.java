package io.snice.networking.common.event;


import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.event.impl.IOEventImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ConnectionActiveIOEvent<T> extends ConnectionIOEvent<T> {
    @Override
    default boolean isConnectionActiveIOEvent() {
        return true;
    }

    /**
     * Indicates whether this connection was initiated by an inbound event. I.e. someone
     * externally to us tried to establish a connection with us.
     */
    boolean isInboundConnection();

    /**
     * Indicates whether this connection was initiated by us. I.e., we were the ones trying to e.g.
     * establish a TCP connection to a remote host.
     */
    default boolean isOutboundConnection() {
        return !isInboundConnection();
    }

    static <T> ConnectionActiveIOEvent<T> create(final ChannelContext<T> ctx, final boolean isInbound, final long arrivalTime) {
        return new ConnectionActiveIOEventImpl(ctx, isInbound, arrivalTime);
    }

    class ConnectionActiveIOEventImpl<T> extends IOEventImpl<T> implements ConnectionActiveIOEvent<T> {
        private final boolean isInboud;
        private ConnectionActiveIOEventImpl(final ChannelContext<T> ctx, final boolean isInbound, final long arrivalTime) {
            super(ctx, arrivalTime);
            this.isInboud = isInbound;
        }

        @Override
        public boolean isInboundConnection() {
            return isInboud;
        }
    }
}
