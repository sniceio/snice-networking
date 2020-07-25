package io.snice.networking.diameter.peer;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.snice.codecs.codec.diameter.avp.api.HostIpAddress;
import io.snice.codecs.codec.diameter.avp.api.ProductName;
import io.snice.networking.common.Transport;
import io.snice.networking.core.NetworkInterface;
import io.snice.networking.diameter.PeerConnection;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * TODO: may actually split the configuration in "public facing", meaning what you actually
 * type in the config file and then an internal one where the internal one is built up based
 * on default + specific settings.
 * <p>
 * Also, may have to split between LocalPeer and RemotePeer configuration objects. Where
 * the RemotePeer will reference a LocalPeer so it knows, essentially, which listen  ip:port
 * it will come in over, or should be connected across.
 * <p>
 * Which then also means that the LocalPeer should reference an Snice Networking Network Interface
 * config. That would, however tie it to the Snice implmentation so perhaps we don't want to do that?
 * Just use ip:port and match it to an interface instead?
 *
 * TODO: convert to builder pattern and make it immutable once built.
 */
public class PeerConfiguration {

    /**
     * Just a unique human friendly name to reference this peer by. This friendly name will
     * actually be used within configuration as well so it has to be matching up
     * to other entries within the config file.
     */
    private String name;

    /**
     * The {@link ProductName} of your product (make something up), which will be conveyed by
     * the {@link PeerConnection} during its CER/CEA handshake (see specification for Capability Exchange Request/Answer
     * for full details. RFC 6733 section 5)
     */
    private Optional<ProductName> productName = Optional.empty();

    /**
     * A list of IP addresses that this peer can be reached across. This is also part of the CER/CEA exchange
     * procedure. When a {@link PeerConnection} runs over SCTP it may in fact be able to be reached across multiple
     * interfaces and as such, IP addresses.
     */
    @JsonProperty("hostIpAddresses")
    private List<HostIpAddress> hostIpAddresses = List.of();

    /**
     * The name of the underlying network interface we will associate this peer with.
     * That means that if this peer is active, it will try to connect to the remote
     * party using this interface. Remember, a network application may have multiple interfaces
     * with multiple ip:port pairs on each. Hence, you must specify which one to use and if you
     * don't, the default one of the underlying network stack will be used.
     */
    @JsonProperty("nic")
    private Optional<String> nic;

    /**
     * A {@link NetworkInterface} can be configured to use multiple {@link Transport}s and as such,
     * we may have to specify the transport to use. If not specified and the {@link NetworkInterface} is
     * only configured with a single protocol (must be tcp, sctp or tls) then that protocol will be used.
     * If the {@link NetworkInterface} is configured to support multiple transport protocols, then we will
     * error out when trying to establish an active peer.
     */
    @JsonProperty("transport")
    private Optional<Transport> transport = Optional.empty();

    @JsonProperty("mode")
    private Peer.MODE mode = Peer.MODE.PASSIVE;

    @JsonProperty("uri")
    private URI uri;

    /**
     * Options for configuring the internal map of outstanding transactions.
     * In general, we would like to avoid re-hashing the internal tables since
     * it can be quite costly, specially at larger sizes. Therefore, ideally, we should
     * figure out an appropriate size so it is highly unlikely that we will ever re-hash.
     * Since that is dependent on a lot of different factors, it is impossible to have
     * a sane default value and therefore, it is configurable per peer.
     * <p>
     * Remember, memory is cheap and typically you don't have that many peers.
     *
     * @return
     */
    public static int getPeerTransactionTableInitialSize() {
        return 100;
    }

    // TODO: each peer should have its own overload policy configured. Or rather, there should
    // be a global one but make sure that gets injected to each peer so we can override it for
    // certain peers. Use case is that one peer may go to a smaller operator and we want to apply a
    // certain set of overload policy towards them v.s. public internet, bigger carrier etc etc.

    public Optional<ProductName> getProductName() {
        return productName;
    }

    public void setProductName(final Optional<ProductName> productName) {
        this.productName = productName == null ? Optional.empty() : productName;
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

    public void setName(final String name) {
        this.name = name;
    }

    public Optional<String> getNic() {
        return nic;
    }

    public void setNic(final Optional<String> nic) {
        this.nic = nic == null ? Optional.empty() : nic;
    }

    public Peer.MODE getMode() {
        return mode;
    }

    public void setMode(final Peer.MODE mode) {
        this.mode = mode;
    }

    public Optional<Transport> getTransport() {
        return transport;
    }

    public void setTransport(final Optional<Transport> transport) {
        this.transport = transport;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(final URI uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "Name: \"" + name + "\"" +
                " Mode: " + mode +
                " ProductName: " + productName.map(ProductName::toString).orElse("N/A") +
                " NIC: " + nic.orElse("N/A");
    }
}
