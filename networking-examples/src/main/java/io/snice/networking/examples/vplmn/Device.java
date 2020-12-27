package io.snice.networking.examples.vplmn;

import io.snice.buffer.Buffer;

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

    /**
     * Ask the device to send the given data to the remote ip:port.
     *
     * If the device is in such a state where it cannot send data, the request
     * will be silently ignored.
     */
    void sendData(final Buffer data, final String remoteIp, int remotePort);
}
