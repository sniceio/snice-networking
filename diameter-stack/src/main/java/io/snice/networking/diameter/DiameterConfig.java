package io.snice.networking.diameter;

import io.snice.codecs.codec.diameter.avp.api.ProductName;
import io.snice.codecs.codec.diameter.avp.api.VendorId;
import io.snice.networking.diameter.peer.PeerConfiguration;

import java.util.ArrayList;
import java.util.List;

public class DiameterConfig {

    private ProductName productName;
    private VendorId vendorId;
    private String productVersion;

    private List<PeerConfiguration> peers = new ArrayList<>();

    public ProductName getProductName() {
        return productName;
    }

    public VendorId getVendorId() {
        return vendorId;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public List<PeerConfiguration> getPeers() {
        return peers;
    }

    public void setPeers(final List<PeerConfiguration> peers) {
        this.peers = peers;
    }

}
