package io.snice.networking.diameter.peer;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.avp.api.HostIpAddress;
import io.snice.codecs.codec.diameter.avp.api.OriginHost;
import io.snice.codecs.codec.diameter.avp.api.OriginRealm;
import io.snice.codecs.codec.diameter.avp.api.ProductName;
import io.snice.networking.common.fsm.NetworkContext;
import io.snice.networking.diameter.PeerConnection;
import io.snice.networking.diameter.event.DiameterEvent;

import java.util.List;
import java.util.Optional;


public interface PeerContext extends NetworkContext<DiameterEvent> {

    /**
     * Get the configuration for this {@link PeerConnection}.
     */
    PeerConfiguration getConfig();

    void sendDownstream(DiameterMessage msg);

    void sendUpstream(DiameterMessage msg);

    default OriginHost getOriginHost() {
        return OriginHost.of("snice.node.epc.mnc001.mcc001.3gppnetwork.org");
    }

    default OriginRealm getOriginRealm() {
        return OriginRealm.of("epc.mnc001.mcc001.3gppnetwork.org");
    }

    default Optional<ProductName> getProductName() {
        return getConfig().getProductName();
    }

    /**
     * The local IP address(s) that this {@link PeerConnection} is reachable over. Every
     * {@link PeerConnection} must have at least one defined. If this {@link PeerConnection}
     * is using an underlying protocol that allows the use of multiple
     * interfaces and multiple IPs, such as SCTP, then there may be
     * may be more than one address in the list (duh).
     *
     * @return
     */
    List<HostIpAddress> getHostIpAddresses();

}
