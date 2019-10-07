package io.snice.networking.event;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ConnectionIOEvent extends IOEvent {

    @Override
    default ConnectionIOEvent toConnectionIOEvent() {
        return this;
    }

    @Override
    default boolean isConnectionIOEvent() {
        return true;
    }

}
