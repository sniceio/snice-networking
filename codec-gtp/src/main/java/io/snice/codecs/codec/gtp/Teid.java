package io.snice.codecs.codec.gtp;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.impl.TeidImpl;

/**
 * Tunnel Endpoint identifier is used to multiplex different connections across
 * the same GTP tunnel.
 */
public interface Teid {

    static Teid of(final Buffer buffer) {
        return TeidImpl.of(buffer);
    }

}
