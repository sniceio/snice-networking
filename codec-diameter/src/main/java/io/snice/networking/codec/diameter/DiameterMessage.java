package io.snice.networking.codec.diameter;

import io.snice.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 */
public interface DiameterMessage {

    DiameterHeader getHeader();

    static DiameterMessage frame(final Buffer buffer) {
        return null;
    }
}
