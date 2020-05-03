package io.snice.codecs.codec.gtp;

public interface GtpRequest extends GtpMessage {

    @Override
    default boolean isRequest() {
        return true;
    }
}
