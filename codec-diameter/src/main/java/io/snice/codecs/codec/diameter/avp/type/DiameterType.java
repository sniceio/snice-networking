package io.snice.codecs.codec.diameter.avp.type;

import io.snice.buffer.WritableBuffer;

public interface DiameterType {

    /**
     * The size of this {@link DiameterType} in bytes. I.e., how many bytes
     * does it take to represent this value when externalized. Typically
     * used when the value is about to get written to e.g. the network.
     */
    int size();

    default void writeValue(final WritableBuffer buffer) {
        throw new RuntimeException("Not implemented for " + getClass().getName() + " just yet");
    }

}
