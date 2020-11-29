package io.snice.networking.gtp;

/**
 * Base class for all GTP related exceptions.
 */
public class GtpException extends RuntimeException {

    public GtpException() {
    }

    public GtpException(final String msg) {
        super(msg);
    }
}
