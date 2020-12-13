package io.snice.networking.gtp.event;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1Message;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1MessageType;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2MessageType;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.DeleteSessionRequest;

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

    default Gtp1Message toGtp1Message() {
        return getMessage().toGtp1Message();
    }

    default Gtp2Message toGtp2Message() {
        return getMessage().toGtp2Message();
    }

    default Gtp2Request toGtp2Request() {
        return getMessage().toGtp2Request();
    }

    default CreateSessionRequest toCreateSessionRequest() {
        return getMessage().toCreateSessionRequest();
    }

    default DeleteSessionRequest toDeleteSessionRequest() {
        return getMessage().toDeleteSessionRequest();
    }

    default boolean isPdu() {
        return getMessage().getHeader().getMessageTypeDecimal() == Gtp1MessageType.G_PDU.getType();
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
