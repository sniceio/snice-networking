package io.snice.networking.gtp;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.networking.gtp.impl.DefaultEpsBearer;
import io.snice.networking.gtp.impl.InternalGtpUserTunnel;

/**
 * Represents an established {@link Bearer} and is, simply put, the
 * established IP path between a UE (user equipment - the customer's modem/IoT device/phone)
 * and an external IP network, such as the public Internet.
 * <p>
 * This interface allows you to send raw data to any endpoint but it also means that any
 * potential data coming back, you (the application) have to deal with yourself, which includes
 * transaction matching (if the protocol/data you're sending supports some sort of transaction),
 * transaction timeouts, re-transmission detection etc. However, if you wish to have the
 * GTP stack handle this, simple create a {@link DataTunnel} between the UE and a particular
 * endpoint (identified by a remote address), which will allow you to defer the "heavy lifting"
 * of transactions decoding/encoding of your data to this tunnel.
 */
public interface EpsBearer {

    static EpsBearer create(final GtpUserTunnel tunnel, final PdnSessionContext ctx, final int defaultLocalPort) {
        try {
            final var internalTunnel = (InternalGtpUserTunnel) tunnel;
            return DefaultEpsBearer.create(internalTunnel, ctx, defaultLocalPort);
        } catch (final ClassCastException e) {
            // If we are unable to convert the given GtpUserTunnel to an internal one, the user must
            // have created their own tunnel outside the GtpSTack since it will only create and deal
            // with internal tunnels.
            throw new IllegalArgumentException("The GtpUserTunnel that was passed in isn't managed by this stack. " +
                    "Did you create it in some other external context?");
        }
    }

    static EpsBearer create(final GtpUserTunnel tunnel, final Buffer assignedDeviceIp, final Bearer localBearer, final Bearer remoteBearer, final int defaultLocalPort) {
        try {
            final var internalTunnel = (InternalGtpUserTunnel) tunnel;
            return DefaultEpsBearer.create(internalTunnel, assignedDeviceIp, localBearer, remoteBearer, defaultLocalPort);
        } catch (final ClassCastException e) {
            throw new IllegalArgumentException("The GtpUserTunnel that was passed in isn't managed by this stack. " +
                    "Did you create it in some other external context?");
        }
    }

    /**
     * Create a new {@link DataTunnel} that is "connected" to the remote host:port and is used
     * to send data of the given type across it.
     *
     * @param type       the type of data that we will be sending across this tunnel.
     * @param remoteHost the remote host (IP or FQDN)
     * @param port       the remote port.
     * @return a builder object to build/configure the tunnel
     */
    <T> DataTunnel.Builder<T> createDataTunnel(Class<T> type, String remoteHost, int port);

    Bearer getLocalBearer();

    /**
     * Get the {@link Teid} for the local {@link Bearer}
     * <p>
     * This is just a convenience method for <code> getLocalBearer().getTeid(); </code>
     */
    default Teid getLocalBearerTeid() {
        return getLocalBearer().getTeid();
    }

    Bearer getRemoteBearer();

    default Teid getRemoteBearerTeid() {
        return getRemoteBearer().getTeid();
    }

    void send(String remoteAddress, int remotePort, Buffer data);

    void send(String remoteAddress, int remotePort, String data);

}
