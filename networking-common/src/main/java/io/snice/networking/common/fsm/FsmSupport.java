package io.snice.networking.common.fsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FsmSupport<S extends Enum<S>> {

    private final Logger logger;

    public FsmSupport(final Class clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    public void unhandledEvent(final S state, final Object o) {
        logger.warn("{} Unhandled event of type {}. Formatted output {}", state, o.getClass().getName(), o);
    }

    public void onTransition(final S currentState, final S toState, final Object event) {
        if (currentState != toState) {
            logger.info("{} -> {} Event: {}", currentState, toState, event);
        }
    }
}
