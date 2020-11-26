package io.snice.networking.common.fsm;

import io.hektor.fsm.Data;
import io.hektor.fsm.FSM;
import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

import java.util.Optional;

public interface FsmFactory<T, S extends Enum<S>, C extends NetworkContext<T>, D extends Data> {

    /**
     * Calculate a new key that uniquely identifies a state machine instance.
     * Depending on what type of state machine, and most likely on the protocol,
     * one of the two parameters may not be taken into consideration when calculate
     * the key. That is up to the implementation.
     * <p>
     * E.g., perhaps you are building an FSM that keeps track of some TCP state. That
     * FSM will most likely only use the {@link ConnectionId} as the key and what
     * message was received across the TCP connection may not matter.
     * <p>
     * Also, it may be that some messages are not meant for this FSM and in that case, you can
     * return null and the invocation of an FSM will be skipped.
     *
     * @param connectionId the id of the underlying {@link Connection}
     * @param msg          an optional message.
     * @return a key that uniquely identifies a particular state machine instance.
     */
    FsmKey calculateKey(ConnectionId connectionId, Optional<T> msg);

    D createNewDataBag(FsmKey key);

    C createNewContext(FsmKey key, ChannelContext<T> ctx);

    FSM<S, C, D> createNewFsm(FsmKey key, C context, D databag);
}
