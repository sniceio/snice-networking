package io.snice.networking.diameter.event;

import io.snice.codecs.codec.diameter.DiameterAnswer;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.DiameterRequest;

public interface DiameterEvent {

    default boolean isRequest() {
        return false;
    }

    default boolean isAnswer() {
        return false;
    }

    default boolean isULR() {
        return false;
    }

    default boolean isULA() {
        return false;
    }

    default boolean isDWR() {
        return false;
    }

    default boolean isDWA() {
        return false;
    }

    default boolean isDPR() {
        return false;
    }

    default boolean isDPA() {
        return false;
    }

    default boolean isCER() {
        return false;
    }

    default boolean isCEA() {
        return false;
    }


    default DiameterMessage getMessage() {
        return toMessageEvent().getMessage();
    }

    default DiameterRequest getRequest() {
        return toMessageEvent().getRequest();
    }

    default DiameterAnswer getAnswer() {
        return toMessageEvent().getAnswer();
    }


    default boolean isMessageReadEvent() {
        return false;
    }

    default DiameterMessageReadEvent toMessageReadEvent() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + DiameterMessageReadEvent.class.getName());
    }

    default boolean isMessageWriteEvent() {
        return false;
    }

    default DiameterMessageWriteEvent toMessageWriteEvent() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + DiameterMessageWriteEvent.class.getName());
    }

    default boolean isMessageEvent() {
        return false;
    }

    default DiameterMessageEvent toMessageEvent() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + DiameterMessageEvent.class.getName());
    }

}
