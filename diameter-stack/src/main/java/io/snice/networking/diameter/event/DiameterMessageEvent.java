package io.snice.networking.diameter.event;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.networking.diameter.tx.Transaction;

import java.util.Optional;

public interface DiameterMessageEvent extends DiameterEvent {

    /**
     * A {@link DiameterMessage} may optionally associated with a {@link Transaction}.
     * The application, upon receiving an initial {@link DiameterRequest} can choose
     * to reply within a transaction or not. The initial {@link DiameterRequest} will
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
    default DiameterMessageEvent toMessageEvent() {
        return this;
    }

}
