package io.snice.networking.gtp;

import io.snice.codecs.codec.gtp.GtpMessage;

import static io.snice.preconditions.PreConditions.assertNull;

/**
 * The given {@link GtpMessage} was not allowed in the context. This could e.g. happen
 * if you try and create a {@link Transaction} for a message type that doesn't expect
 * an answer and as such, cannot be run within a {@link Transaction}.
 */
public class IllegalGtpMessageException extends GtpException {

    private final GtpMessage offendingMessage;

    public static IllegalGtpMessageException of(final GtpMessage offendingMessage) {
        assertNull(offendingMessage);
        return new IllegalGtpMessageException(offendingMessage);
    }

    public static IllegalGtpMessageException of(final GtpMessage offendingMessage, final String msg) {
        assertNull(offendingMessage);
        return new IllegalGtpMessageException(offendingMessage, msg);
    }

    private IllegalGtpMessageException(final GtpMessage offendingMessage) {
        super("The GTP Message " + offendingMessage.getHeader() + " is illegal in the given context");
        this.offendingMessage = offendingMessage;
    }

    private IllegalGtpMessageException(final GtpMessage offendingMessage, final String msg) {
        super(msg);
        this.offendingMessage = offendingMessage;
    }

    public GtpMessage getOffendingMessage() {
        return offendingMessage;
    }
}
