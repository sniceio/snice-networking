/**
 * 
 */
package io.snice.networking.common;

import io.snice.buffer.Buffer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;

/**
 * Represents a connection between two end-points and its primary purpose is to
 * encapsulate specific knowledge of which type of underlying implementation is
 * being used.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Connection<T> {

    ConnectionId id();

    /**
     * A connection may optionally have a VIP address, which for the
     * actual connection itself doesn't matter but there are cases
     * where you e.g. want to stamp a different address, a VIP address,
     * in the Via and Contact-headers of your SIP message. This is
     * common when you have some sort of load balancer or your machine
     * is NAT:ed and therefore, you want to have that external facing
     * address stamped instead.
     *
     * @return
     */
    Optional<URI> getVipAddress();

    /**
     * Get the local port to which this {@link Connection} is listening to.
     * 
     * @return
     */
    int getLocalPort();

    /**
     * Just a convenience method for obtaining the default port for this
     * type of connection. If the connection represents a UDP "connection" or
     * a TCP connection then 5060 will be returned. If the connection is
     * TLS then 5061 will be returned.
     *
     * @return
     */
    int getDefaultPort();

    /**
     * Get the local ip-address to which this {@link Connection} is listening to
     * as a byte-array.
     * 
     * @return
     */
    byte[] getRawLocalIpAddress();

    /**
     * Get the local ip-address to which this {@link Connection} is listening to
     * as a {@link String}.
     * 
     * @return
     */
    String getLocalIpAddress();

    InetSocketAddress getLocalAddress();

    Buffer getLocalIpAddressAsBuffer();

    /**
     * Get the remote address to which this {@link Connection} is connected to.
     * 
     * @return
     */
    InetSocketAddress getRemoteAddress();

    /**
     * Get the remote port to which this {@link Connection} is connected to.
     * 
     * @return
     */
    int getRemotePort();

    /**
     * Get the remote ip-address to which this {@link Connection} is connected
     * to as a byte-array.
     * 
     * @return
     */
    byte[] getRawRemoteIpAddress();

    /**
     * Get the remote ip-address to which this {@link Connection} is connected
     * to as a {@link String}.
     * 
     * @return
     */
    String getRemoteIpAddress();

    Buffer getRemoteIpAddressAsBuffer();

    Transport getTransport();

    /**
     * Check whether or not this {@link Connection} is using UDP as its
     * underlying transport protocol.
     * 
     * @return
     */
    default boolean isUDP() {
        return getTransport().isUDP();
    }

    /**
     * Check whether or not this {@link Connection} is using TCP as its
     * underlying transport protocol.
     * 
     * @return
     */
    default boolean isTCP() {
        return getTransport().isTCP();
    }

    /**
     * Check whether or not this {@link Connection} is using TLS as its
     * underlying transport protocol.
     * 
     * @return
     */
    default boolean isTLS() {
        return getTransport().isTLS();
    }

    /**
     * Check whether or not this {@link Connection} is using SCTP as its
     * underlying transport protocol.
     * 
     * @return
     */
    default boolean isSCTP() {
        return getTransport().isSCTP();
    }

    /**
     * Check whether or not this {@link Connection} is using websocket as its
     * underlying transport protocol.
     * 
     * @return
     */
    default boolean isWS() {
        return getTransport().isWS();
    }

    /**
     * Check whether or not this {@link Connection} is using secure websocket as its
     * underlying transport protocol.
     */
    default boolean isWSS() {
        return getTransport().isWSS();
    }

    /**
     * Send an Object over this connection.
     * 
     * @param msg
     */
    void send(T msg);

    boolean connect();

    void close();

}
