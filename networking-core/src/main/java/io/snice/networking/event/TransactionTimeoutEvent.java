package io.snice.networking.event;

/**
 * Created by jonas on 7/22/17.
 */
public interface TransactionTimeoutEvent extends IOEvent {

    @Override
    default boolean isTimeoutEvent() {
        return true;
    }

}
