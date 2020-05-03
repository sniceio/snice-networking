package io.snice.codecs.codec;

public interface SerializationFactory<T> {

    Framer<T> getFramer();
}
