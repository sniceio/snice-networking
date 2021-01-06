package io.snice.networking.examples.vplmn;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.Imei;

public interface Device {

    Imei getImei();

    SimCard getSimCard();

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
     * <p>
     * If the device is in such a state where it cannot send data, the request
     * will be silently ignored.
     */
    void sendData(final Buffer data, final String remoteIp, int remotePort);

    default void sendData(final String data, final String remoteIp, int remotePort) {
        sendData(Buffers.wrap(data), remoteIp, remotePort);
    }

    enum Type {
        IPHONE, ANDROID, BG96;
    }
}
