package io.snice.networking.gtp.event;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2MessageType;

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

    default GtpMessage getMessage() {
        return toMessageEvent().getMessage();
    }

    default Gtp2Message toGtp2Message() {
        return getMessage().toGtp2Message();
    }

    default boolean isCreateSessionRequest() {
        return getMessage().getHeader().getMessageTypeDecimal() == Gtp2MessageType.CREATE_SESSION_REQUEST.getType();
    }

    default boolean isCreateSessionResponse() {
        return getMessage().getHeader().getMessageTypeDecimal() == Gtp2MessageType.CREATE_SESSION_RESPONSE.getType();
    }

    default boolean isDeleteSessionRequest() {
        return getMessage().getHeader().getMessageTypeDecimal() == Gtp2MessageType.DELETE_SESSION_REQUEST.getType();
    }

    default boolean isDeleteSessionResponse() {
        return getMessage().getHeader().getMessageTypeDecimal() == Gtp2MessageType.DELETE_SESSION_RESPONSE.getType();
    }

    default boolean isEchoRequest() {
        return getMessage().getHeader().getMessageTypeDecimal() == Gtp2MessageType.ECHO_REQUEST.getType();
    }

    default boolean isEchoResponse() {
        return getMessage().getHeader().getMessageTypeDecimal() == Gtp2MessageType.ECHO_RESPONSE.getType();
    }
}
