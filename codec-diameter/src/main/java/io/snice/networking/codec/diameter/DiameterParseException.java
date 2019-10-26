/**
 *
 */
package io.snice.networking.codec.diameter;


/**
 * @author jonas@jonasborjesson.com
 */
public class DiameterParseException extends DiameterException {

    private final int errorOffset;
    private final String template;

    public DiameterParseException(final int errorOffset, final String message) {
        super(String.format(message, errorOffset));
        this.errorOffset = errorOffset;
        template = message;
    }

    public DiameterParseException(final String message) {
        this(0, message);
    }

    public DiameterParseException(final int errorOffset, final String message, final Exception cause) {
        super(String.format(message, errorOffset), cause);
        this.errorOffset = errorOffset;
        template = message;
    }

    public int getErrorOffset() {
        return errorOffset;
    }

    public String getTemplate() {
        return template;
    }
}
