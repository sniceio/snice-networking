package io.snice.networking.codec.gtp;

public interface GtpResponse extends GtpMessage {

    default boolean isResponse() {
        return true;
    }
}
