package io.snice.networking.diameter;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.diameter.peer.PeerException;
import io.snice.networking.diameter.peer.RoutingEngine;

import java.util.Optional;

/**
 * When the {@link RoutingEngine} tries to determine the appropriate route for a given
 * {@link DiameterMessage} but is unable to do so, this exception, and sub-classes of it, will
 * be thrown to indicate a routing issue.
 */
public class DiameterRoutingException extends PeerException {

    private final Optional<DiameterMessage> msg;

    public DiameterRoutingException(final DiameterMessage diameter, final String msg) {
        super(msg);
        this.msg = Optional.ofNullable(diameter);
    }

    public DiameterRoutingException(final String msg) {
        super(msg);
        this.msg = Optional.empty();
    }

    /**
     * Get the, optional, {@link DiameterMessage} for which this exception was generated.
     */
    public Optional<DiameterMessage> getDiameterMessage() {
        return msg;
    }

}
