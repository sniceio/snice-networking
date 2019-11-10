package io.snice.networking.common.event;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ConnectionIOEvent<T> extends IOEvent<T> {

    @Override
    default ConnectionIOEvent<T> toConnectionIOEvent() {
        return this;
    }

    @Override
    default boolean isConnectionIOEvent() {
        return true;
    }

}
