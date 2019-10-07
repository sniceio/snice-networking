package io.snice.networking.common;


import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;

import java.io.IOException;

/**
 * Note, enums should be all caps but SIP is annoying and for transports in a SipURI the transport
 * is supposed to be lower case so therefore we just made these into lower case as well. Just easier
 * that way.
 * <p>
 * Created by jonas@jonasborjesson.com
 */
public enum Transport {
    udp(Buffers.wrap("udp"), false),
    tcp(Buffers.wrap("tcp"), true),
    tls(Buffers.wrap("tls"), true),
    sctp(Buffers.wrap("sctp"), true),
    ws(Buffers.wrap("ws"), true),
    wss(Buffers.wrap("wss"), true);

    final Buffer buffer;
    final Buffer upperCaseBuffer;
    final boolean isReliable;

    Transport(final Buffer buffer, final boolean isReliable) {
        this.buffer = buffer;
        this.upperCaseBuffer = Buffers.wrap(buffer.toString().toUpperCase());
        this.isReliable = isReliable;
    }

    public boolean isReliable() {
        return isReliable;
    }

    public boolean isUDP() {
        return this == udp;
    }

    public boolean isTCP() {
        return this == tcp;
    }

    public boolean isTLS() {
        return this == tls;
    }

    public boolean isSCTP() {
        return this == sctp;
    }

    public boolean isWS() {
        return this == ws;
    }

    public boolean isWSS() {
        return this == wss;
    }

    /**
     * Get the transport off of the given buffer
     *
     * @param buffer
     * @return the transport
     * @throws IllegalArgumentException in case the supplied buffer isn't a recognized transport
     */
    public static Transport of(final Buffer buffer) throws IllegalArgumentException {
        if (buffer == null || buffer.isEmpty()) {
            throw new IllegalArgumentException("Illegal Transport - the transport buffer cannot be null or empty");
        }

        if (isUDP(buffer) || isUDPLower(buffer)) {
            return udp;
        }

        if (isTCP(buffer) || isTCPLower(buffer)) {
            return tcp;
        }

        if (isTLS(buffer) || isTLSLower(buffer)) {
            return tls;
        }

        if (isSCTP(buffer) || isSCTPLower(buffer)) {
            return sctp;
        }

        if (isWS(buffer) || isWSLower(buffer)) {
            return ws;
        }

        if (isWSS(buffer) || isWSSLower(buffer)) {
            return wss;
        }

        throw new IllegalArgumentException("Illegal Transport - Unknown transport \"" + buffer + "\"");
    }

    public static Transport of(final String buffer) throws IllegalArgumentException {
        if (buffer == null || buffer.isEmpty()) {
            throw new IllegalArgumentException("Illegal Transport - the transport buffer cannot be null or empty");
        }
        return of(Buffers.wrap(buffer));
    }

    public Buffer toBuffer() {
        return buffer;
    }

    public Buffer toUpperCaseBuffer() {
        return upperCaseBuffer;
    }

    /**
     * Check whether the buffer is exactly three bytes long and has the bytes "UDP" in it.
     *
     * @param t
     * @return
     */
    public static boolean isUDP(final Buffer t) {
        return t.capacity() == 3 && t.getByte(0) == 'U' && t.getByte(1) == 'D' && t.getByte(2) == 'P';
    }

    public static boolean isTCP(final Buffer t) {
        return t.capacity() == 3 && t.getByte(0) == 'T' && t.getByte(1) == 'C' && t.getByte(2) == 'P';
    }

    public static boolean isTLS(final Buffer t) {
        return t.capacity() == 3 && t.getByte(0) == 'T' && t.getByte(1) == 'L' && t.getByte(2) == 'S';
    }

    public static boolean isWS(final Buffer t) {
        return t.capacity() == 2 && t.getByte(0) == 'W' && t.getByte(1) == 'S';
    }

    public static boolean isWSS(final Buffer t) {
        return t.capacity() == 3 && t.getByte(0) == 'W' && t.getByte(1) == 'S' && t.getByte(2) == 'S';
    }

    public static boolean isSCTP(final Buffer t) {
        return t.capacity() == 4 && t.getByte(0) == 'S' && t.getByte(1) == 'C' && t.getByte(2) == 'T'
                && t.getByte(3) == 'P';
    }

    /**
     * Check whether the buffer is exactly three bytes long and has the
     * bytes "udp" in it. Note, in SIP there is a different between transport
     * specified in a Via-header and a transport-param specified in a SIP URI.
     * One is upper case, one is lower case. Another really annoying thing
     * with SIP.
     *
     * @param t
     * @return
     */
    public static boolean isUDPLower(final Buffer t) {
        return t.capacity() == 3 && t.getByte(0) == 'u' && t.getByte(1) == 'd' && t.getByte(2) == 'p';
    }

    public static boolean isTCPLower(final Buffer t) {
        return t.capacity() == 3 && t.getByte(0) == 't' && t.getByte(1) == 'c' && t.getByte(2) == 'p';
    }

    public static boolean isTLSLower(final Buffer t) {
        return t.capacity() == 3 && t.getByte(0) == 't' && t.getByte(1) == 'l' && t.getByte(2) == 's';
    }

    public static boolean isWSLower(final Buffer t) {
        return t.capacity() == 2 && t.getByte(0) == 'w' && t.getByte(1) == 's';
    }

    public static boolean isWSSLower(final Buffer t) {
        return t.capacity() == 3 && t.getByte(0) == 'w' && t.getByte(1) == 's' && t.getByte(2) == 's';
    }

    public static boolean isSCTPLower(final Buffer t) {
        return t.capacity() == 4 && t.getByte(0) == 's' && t.getByte(1) == 'c' && t.getByte(2) == 't'
                && t.getByte(3) == 'p';
    }

}
