package io.snice.networking.diameter.peer;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.diameter.DiameterRoutingException;

/**
 * When the {@link RoutingEngine} tries to find an appropriate {@link Peer} to use for
 * sending the {@link DiameterMessage} but is unable to find one, this exception will be thrown.
 */
public class NoMatchingPeerException extends DiameterRoutingException {

    public NoMatchingPeerException(final DiameterMessage msg) {
        super(msg, "No matching peer found for the given diameter message.");
    }
}
