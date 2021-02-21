package io.snice.networking.gtp;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.common.Connection;
import io.snice.networking.gtp.event.GtpEvent;

/**
 * Base tunnel representing the flow between two ip:port pairs.
 */
public interface GtpTunnel extends Connection<GtpEvent> {

    default boolean isControlTunnel() {
        return false;
    }

    default GtpControlTunnel toControlTunnel() {
        throw new ClassCastException("Cannot cast " + getClass().getName() + " into a " + GtpControlTunnel.class.getName());
    }

    void send(GtpMessage msg);
}
