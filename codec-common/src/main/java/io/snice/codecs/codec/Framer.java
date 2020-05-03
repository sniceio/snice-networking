package io.snice.codecs.codec;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;

import java.util.Optional;

public interface Framer<T> {

    /**
     * Try and frame the content in the {@link Buffer}. If successful, an {@link Optional}
     * with the framed object will be returned. If the {@link Buffer} did not contain
     * all the necessary data to successfully frame the object, then an empty {@link Optional} must be
     * returned, indicating that all is well and hopefully future calls to {@link #frame(Buffer)} will
     * eventually result in a fully framed object.
     *
     * If something goes wrong, an exception should be thrown.
     *
     * @param buffer
     * @return an {@link Optional} where an empty optional indicates that all is well but we do not
     * currently have enough data to frame the object but that future calls to this method may eventually
     * yield in a fully framed object. If the optional isn't empty then we did indeed successfully
     * framed a new object.
     */
    Optional<T> frame(ReadableBuffer buffer);
}
