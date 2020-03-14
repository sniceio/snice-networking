package io.snice.networking.diameter.peer;

import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.avp.api.HostIpAddress;
import io.snice.networking.common.fsm.NetworkContext;
import io.snice.networking.diameter.Peer;

import java.util.List;


public interface PeerContext extends NetworkContext<DiameterMessage> {

    /**
     * Get the configuration for this {@link Peer}.
     */
    PeerConfiguration getConfig();

    /**
     * The local IP address(s) that this {@link Peer} is reachable over. Every
     * {@link Peer} must have at least one defined. If this {@link Peer}
     * is using an underlying protocol that allows the use of multiple
     * interfaces and multiple IPs, such as SCTP, then there may be
     * may be more than one address in the list (duh).
     *
     * @return
     */
    List<HostIpAddress> getHostIpAddresses();

}
