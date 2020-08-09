package io.snice.networking.diameter.event;

public interface DiameterEvent {


    default boolean isMessageEvent() {
        return false;
    }

    default DiameterMessageEvent toMessageEvent() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + DiameterMessageEvent.class.getName());
    }

}
