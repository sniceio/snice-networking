package io.snice.networking.diameter.peer;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.snice.networking.codec.diameter.avp.api.HostIpAddress;
import io.snice.networking.codec.diameter.avp.api.ProductName;

import java.util.List;

public class PeerConfiguration {

    private String name;

    private ProductName productName;

    @JsonProperty("hostIpAddresses")
    private List<HostIpAddress> hostIpAddresses = List.of();

    /**
     * Options for configuring the internal map of outstanding transactions.
     * In general, we would like to avoid re-hashing the internal tables since
     * it can be quite costly, specially at larger sizes. Therefore, ideally, we should
     * figure out an appropriate size so it is highly unlikely that we will ever re-hash.
     * Since that is dependent on a lot of different factors, it is impossible to have
     * a sane default value and therefore, it is configurable per peer.
     *
     * Remember, memory is cheap and typically you don't have that many peers.
     *
     * @return
     */
    public static int getPeerTransactionTableInitialSize() {
        return 100;
    }

    public ProductName getProductName() {
        return productName;
    }

    public void setProductName(final ProductName productName) {
        this.productName = productName;
    }

    public List<HostIpAddress> getHostIpAddresses() {
        return hostIpAddresses;
    }

    public void setHostIpAddresses(final List<HostIpAddress> hostIpAddresses) {
        this.hostIpAddresses = hostIpAddresses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
