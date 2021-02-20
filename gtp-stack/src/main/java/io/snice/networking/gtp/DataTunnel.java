package io.snice.networking.gtp;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1Request;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A {@link DataTunnel} is simply a helper/convenience class to aid the application built on top of the
 * GTP Stack to manage messages between the UE and a remote endpoint. The "raw" {@link EpsBearer} allows the
 * application to send & receive any data between the UE and an external IP network but does little in
 * helping the application with protocols that are transactional, which many of them are. Hence, if your
 * application is sending data over the established {@link Bearer} that is transactional in nature (DNS,
 * SIP, HTTP, <your own> - almost any protocol that has a request/response), then the you can create
 * a {@link DataTunnel} to help you manage that.
 * <p>
 * Note: using this {@link DataTunnel} comes with the assumption that you only send one type of
 * message over that tunnel. I.e., if you "establish" a tunnel over a particular {@link Bearer}
 * to Google's DNS servers on port 53 (so e.g. 8.8.8.8:53) and you configure the tunnel to deal with
 * DNS traffic, it is assumed that ONLY DNS traffic is sent to/from the remote endpoint (the underlying
 * implementation will have to match on something for the incoming data so it nodes which decoder to use
 * for the raw data).
 *
 * @param <T>
 */
public interface DataTunnel<T> {

    Optional<Object> getUserData();

    interface Builder<T> {

        /**
         * In order to be able to exchange IP traffic between the UE and the external IP network (such as the
         * public Internet), a bearer must first be established. This is accomplished by establishing a "PDN Session"
         * and is done over the GTP Control Tunnel by exchanging a Create Session Request/Response, which will
         * include information of the {@link Teid} to use, what address was allocated etc.
         * <p>
         * The {@link DataTunnel} must know the local and remote {@link Teid} since when the {@link DataTunnel}
         * constructs a PDU to send across the underlying {@link GtpUserTunnel}, it must include the {@link Teid}
         * in the {@link Gtp1Request} (the PDU, which is just a GTPv1 message of type PDU (type value = 255))
         */
        Builder<T> withRemoteTeid(Teid teid);

        /**
         * Specify the local {@link Teid}. For full explanation, see {@link #withLocalTeid(Teid)}
         */
        Builder<T> withLocalTeid(Teid teid);

        /**
         * When a {@link Bearer} is established, the PGW will allocate and assign an IP address
         * to the UE and that IP must be used when crafting the IP packets from the device, which is
         * subsequently sent across the GTP-U tunnel (within a PDU).
         * <p>
         * Note: since this builder does not check if there truly exists an established {@link Bearer} or not, you
         * can really put whatever you want here. If you want a real application that works through a real PGW, then
         * most likely this won't work because they PGW may check and drop it. However, if you perhaps are building
         * a testing tool to test a PGW, perhaps you do want to put any random IP address here.
         *
         * @param deviceIp an IPv4 IP encoded as a {@link Buffer} and as such, is expected to be an encoded 4 byte
         *                 value (essentially the IPv4 address is encoded as an Integer)
         * @return
         */
        Builder<T> withLocalIPv4DeviceIp(Buffer deviceIp);

        /**
         * When the UE is sending data across the established {@link Bearer}, the IP address used is what is
         * assigned by the PGW when the session is established. However, the port is up to the UE and may
         * change at any given point in time. When you create this {@link DataTunnel}, it will be using
         * this port always (if you want to send from another port, you have to create another {@link DataTunnel}.
         *
         * @param port
         */
        Builder<T> withLocalPort(int port);

        /**
         * Unless the type is a {@link Buffer} you must supply a decoder to decode the raw buffer
         * into an instance of the given type.
         */
        Builder<T> withDecoder(Function<Buffer, T> decoder);

        /**
         * If you, the application, wish to register some object with the tunnel to allow you to
         * perhaps map/identify the tunnel with this data, you can do so here. This application
         * data blob is 100% transparent to the actual {@link DataTunnel} and is not used
         * by it in any way other than allowing the application to retrieve it through
         * {@link DataTunnel#getUserData()}.
         * <p>
         * NOTE: if you pass in an mutable object there are not guarantees that any changes to that
         * object will be reflected in any of the callbacks since it is not guaranteed that
         * the same thread is used in subsequent calls. In general, don't have mutable data but if you
         * must, you have to pass in a thread safe object.
         */
        Builder<T> withUserData(Object data);

        /**
         * The data your application is sending to the remote endpoint must be serialized
         * into a raw byte-array, as represented by the {@link Buffer}. Hence, you must
         * supply an encoder to accomplish this.
         *
         * @param encoder
         * @return
         */
        Builder<T> withEncoder(Function<T, Buffer> encoder);

        /**
         * Register a callback that will be invoked for any data that
         * is received over this "tunnel".
         * <p>
         * Note: if you sent some data in a {@link UserDataTransaction} and the incoming data
         * is matched against an outstanding transaction, then the registered callbacks associated
         * with that transaction will be invoked.
         *
         * @param f
         * @return
         */
        Builder<T> onData(BiConsumer<DataTunnel<T>, T> f);

        /**
         * @throws IllegalArgumentException in case any values are missing, or otherwise misconfigured.
         */
        DataTunnel<T> build() throws IllegalArgumentException;
    }
}
