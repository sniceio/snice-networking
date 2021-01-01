package io.snice.networking.examples.vplmn.fsm.devices.device;

public enum DeviceState {

    /**
     * Device has been turned off.
     */
    OFFLINE,

    /**
     * Attempting to authenticate the device, meaning we'll be sending an AIR
     * and are awaiting an AIA.
     */
    AUTHENTICATING,

    /**
     * We have successfully been authenticated, which is either through successfully
     * receiving an AIA or we were "pre-authed", which means we will not issue an AIR/AIA
     * "handshake".
     */
    AUTHENTICATED,

    /**
     * Attempting to attach to the network, meaning we are issuing a diameter ULR and are
     * awaiting for a response. If successful we'll be attached.
     */
    ATTACHING,

    /**
     * We are successfully attached to the network, which means we successfully did a ULR/ULA "handshake"
     * or we were "pre-attached", meaning we skipped the ULR/ULA exchange.
     */
    ATTACHED,

    /**
     * Attempt to initiate a new PDN Session and if successful, we will have established a session.
     */
    INITIATE_SESSION,

    /**
     * We have successfully established a PDN Session, at which point we have one or more
     * bearer established.
     * <p>
     * This state is a transient state and will transition to ONLINE.
     */
    SESSION_ESTABLISHED,

    /**
     * We may have one or more bearer established and as part of the initial PDN Session
     * establishment, we will establish a default bearer. Hence, we actually go
     * INITIATE_SESSION -> ESTABLISH_BEARER -> SESSION_ESTABLISHED -> ONLINE
     */
    ESTABLISHING_BEARER,

    /**
     * Device has successfully registered with the network and has
     */
    ONLINE,

    /**
     * All FSMs need a final state and I guess this one represents when you smashed your
     * phone beyond repair :-)
     */
    DEAD;
}
