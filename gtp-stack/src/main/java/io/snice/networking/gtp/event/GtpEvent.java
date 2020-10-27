package io.snice.networking.gtp.event;

public interface GtpEvent {

    default boolean isMessageReadEvent() {
        return false;
    }

    default GtpMessageReadEvent toMessageReadEvent() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + GtpMessageReadEvent.class.getName());
    }

    default boolean isMessageWriteEvent() {
        return false;
    }

    default GtpMessageWriteEvent toMessageWriteEvent() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + GtpMessageWriteEvent.class.getName());
    }

    default boolean isMessageEvent() {
        return false;
    }

    default GtpMessageEvent toMessageEvent() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + GtpMessageEvent.class.getName());
    }
}
