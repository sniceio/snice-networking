package io.snice.networking.diameter.peer;

import io.snice.networking.diameter.Peer;

/**
 * See RFC6733 5.6. Peer State Machine for the various states and their definition.
 */
public enum PeerState {
    /**
     * Actually the initial state of the Peer FSM. All Peers start off, when created, in the
     * CLOSED state. Also, according to specification, CLOSED is one of the stable states
     * for the Peer state machine. The other stable state being {@link PeerState#OPEN}.
     */
    CLOSED,

    /**
     * The stable state for which the {@link Peer} will accept normal diameter messages
     * and if accepted, will pass it up to the application for further processing.
     *
     * This is one of two stable states for the Peer FSM, the other beeing {@link PeerState#CLOSED}
     */
    OPEN,

    /**
     * This is not a state that is part of the RFC. However, when we receive an incoming connection
     * we know that we must receive a CER soon and it not, we should just close down the
     * connection since it could be a sign of an attack. So, this is an extra intermediary step
     * to the FSM that makes this FSM a bit more resilient.
     */
    WAIT_CER,

    CLOSING, WAIT_CONNECT_ACK, WAIT_CONNECT_ACL_ELECT, WAIT_CEA, WAIT_RETURNS,

    /**
     * Note that the TERMINATED state isn't part of the RFC as a valid state. However,
     * as far as the hektor.io FSM framework, we need to have a final state, which cannot
     * be the same as the initial state, which is, CLOSED.
     *
     * Also, the execution environment for the FSM must know when the FSM is dead and as
     * such, should be removed from memory.
     */
    TERMINATED
}
