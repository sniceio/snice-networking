package io.snice.networking.codec;

import io.snice.buffer.Buffer;

public interface SerializationFactory<T> {

    Framer<T> getFramer();
}
