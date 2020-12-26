package io.snice.networking.examples.vplmn;

public interface Device {

    String getImei();

    /**
     * Ask the device to go online.
     */
    void goOnline();

    /**
     * Ask the device to go offline
     */
    void goOffline();
}
