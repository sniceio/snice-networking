package io.snice.networking.common.event;

import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.event.impl.IOEventImpl;

public interface MessageIOEvent<T> extends IOEvent<T> {

    @Override
    default boolean isMessageIOEvent() {
        return true;
    }

    @Override
    default MessageIOEvent toMessageIOEvent() {
        return this;
    }

    static <T> MessageIOEvent<T> create(final ChannelContext<T> ctx, final long arrivalTime, final T msg) {
        return new DefaultMessageIOEvent<>(ctx, arrivalTime, msg);
    }

    T getMessage();

    class DefaultMessageIOEvent<T> extends IOEventImpl<T> implements MessageIOEvent<T> {

        private final T msg;

        private DefaultMessageIOEvent(final ChannelContext<T> ctx, final long arrivalTime, final T msg) {
            super(ctx, arrivalTime);
            this.msg = msg;
        }

        @Override
        public T getMessage() {
            return msg;
        }

    }
}
