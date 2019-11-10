package io.snice.networking.diameter.peer;

/**
 * See RFC6733 5.6. Peer State Machine for the various states and their definition.
 */
public enum PeerState {
    /**
     * Actually the initial state of the Peer FSM. All Peers start off, when created, in the
     * CLOSED state.
     */
    CLOSED,
    OPEN, CLOSING, WAIT_CONNECT_ACK, WAIT_CONNECT_ACL_ELECT, WAIT_CEA, WAIT_RETURNS,

    /**
     * Note that the TERMINATED state isn't part of the RFC as a valid state. However,
     * as far as the hektor.io FSM framework, we need to have a final state, which cannot
     * be the same as the initial state, which is, CLOSED.
     */
    TERMINATED;
}
