package io.snice.networking.gtp.event;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.GtpRequest;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.gtp.Transaction;

import java.util.Optional;

public interface GtpMessageEvent extends GtpEvent {

    /**
     * A {@link GtpMessage} may optionally associated with a {@link Transaction}.
     * The application, upon receiving an initial {@link GtpRequest} can choose
     * to reply within a transaction or not. The initial {@link GtpRequest} will
     * always have an empty transaction associated with it and if the application then would
     * like to answer within a transaction, then the application has to
     *
     * @return
     */
    Optional<Transaction> getTransaction();

    @Override
    default boolean isMessageEvent() {
        return true;
    }

    @Override
    default GtpMessageEvent toMessageEvent() {
        return this;
    }

    Connection<GtpEvent> getConnection();

    ConnectionId getConnectionId();

    @Override
    GtpMessage getMessage();
}
