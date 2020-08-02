package io.snice.networking.common.event;

import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.event.impl.IOEventImpl;

public interface ApplicationEvent<T> extends IOEvent<T> {

    static <T> ApplicationEvent<T> create(final ChannelContext<T> ctx, final Object event, final long arrivalTime) {
        return new ApplicationEventImpl<>(ctx, event, arrivalTime);
    }

    @Override
    default boolean isApplicationEvent() {
        return true;
    }

    @Override
    default ApplicationEvent<T> toApplicationEvent() {
        return this;
    }

    Object getApplicationEvent();

    class ApplicationEventImpl<T> extends IOEventImpl<T> implements ApplicationEvent<T> {
        private final Object event;

        private ApplicationEventImpl(final ChannelContext<T> ctx, final Object event, final long arrivalTime) {
            super(ctx, arrivalTime);
            this.event = event;
        }

        @Override
        public Object getApplicationEvent() {
            return event;
        }
    }
}
