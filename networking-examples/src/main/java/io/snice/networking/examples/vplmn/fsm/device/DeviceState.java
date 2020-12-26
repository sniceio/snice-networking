package io.snice.networking.examples.vplmn.fsm.device;

public enum DeviceState {

    /**
     * Device has been turned off.
     */
    OFFLINE,

    INITIATE_PDN_SESSION,

    /**
     * Device has successfully registered with the network.
     */
    ONLINE,

    /**
     * All FSMs need a final state and I guess this one represents when you smashed your
     * phone beyond repair :-)
     */
    DEAD;
}
