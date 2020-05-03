/**
 *
 */
package io.snice.codecs.codec.gtp;


/**
 * @author jonas@jonasborjesson.com
 */
public class UnknownGtp2MessageTypeException extends GtpException {

    private final int type;

    public UnknownGtp2MessageTypeException(final int type) {
        super("Unknown GTPv2-C Message Type " + type);
        this.type = type;
    }

    public int getType() {
        return type;
    }

}
