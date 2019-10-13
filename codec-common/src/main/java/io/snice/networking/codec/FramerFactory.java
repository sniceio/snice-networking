package io.snice.networking.codec;

import io.snice.buffer.Buffer;

public interface FramerFactory<T> {

    Framer<T> getFramer();
}
