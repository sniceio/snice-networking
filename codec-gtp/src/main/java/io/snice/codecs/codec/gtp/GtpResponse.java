package io.snice.codecs.codec.gtp;

public interface GtpResponse extends GtpMessage {

    @Override
    default boolean isResponse() {
        return true;
    }
}
