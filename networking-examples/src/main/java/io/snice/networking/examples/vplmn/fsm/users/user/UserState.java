package io.snice.networking.examples.vplmn.fsm.users.user;

import io.snice.networking.examples.vplmn.Device;

public enum UserState {

    /**
     *
     */
    INIT,

    /**
     * The users's {@link Device} is online but user is not actively using their device.
     */
    IDLE,

    /**
     * The user is booting up their device, which may or may not succeed and will also
     * take an arbitrary amount of time depending on the device.
     */
    BOOTING,

    /**
     * In offline mode the user has either turned their device off or
     * put it into airplane mode. Either or, the device is not connected
     * to the network.
     */
    OFFLINE,

    /**
     * In this mode, the user ise actively surfing the web, or using data in some fashion.
     */
    SURFING,

    /**
     * The user is done done, meaning, the user is no more and the FSM
     * will be terminated and removed.
     */
    DONE;
}
