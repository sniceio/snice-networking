package io.snice.networking.examples.gtp;

import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1Message;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1MessageType;
import io.snice.codecs.codec.gtp.gtpc.v1.impl.ImmutableGtp1Message;
import io.snice.codecs.codec.internet.IpMessage;
import io.snice.codecs.codec.transport.UdpMessage;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.app.buffer.BufferApplication;
import io.snice.networking.app.buffer.BufferEnvironment;
import io.snice.networking.bundles.buffer.BufferConnection;
import io.snice.networking.bundles.buffer.BufferEvent;
import io.snice.networking.bundles.buffer.BufferReadEvent;
import io.snice.networking.common.ConnectionEndpointId;
import io.snice.networking.common.Transport;
import io.snice.networking.core.NetworkInterface;
import io.snice.networking.gtp.GtpTunnel;
import io.snice.networking.gtp.PdnSessionContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import static io.snice.networking.app.NetworkBootstrap.ACCEPT_ALL;

public class Sgi extends BufferApplication<SgiConfig> {

    private BufferEnvironment<SgiConfig> environment;
    // private final ConcurrentMap<ConnectionEndpointId, NatEntry> nats = new ConcurrentHashMap<>();

    // Make sure we don't need to re-hash...
    private final ConcurrentMap<Teid, NatEntry> sessions = new ConcurrentHashMap(10000, 0.75f);
    private final ConcurrentMap<ConnectionEndpointId, NatEntry> nats = new ConcurrentHashMap(10000, 0.75f);

    @Override
    public void initialize(final NetworkBootstrap<BufferConnection, BufferEvent, SgiConfig> bootstrap) {
        bootstrap.onConnection(ACCEPT_ALL).accept(b -> {
            b.match(buffer -> true).map(BufferEvent::toReadEvent).consume(this::processBuffer);
        });
    }

    @Override
    public void run(final SgiConfig configuration, final BufferEnvironment<SgiConfig> environment) {
        this.environment = environment;
    }

    /**
     * Process a PDU packet that arrived over the given {@link GtpTunnel}.
     * <p>
     * What we should do, but currently are not, is to allocate a new socket for this tunnel so all
     * traffic associated with it is correctly NAT:ed and tunneled back to it. However, currently this
     * is a simple SGi example and as such, if two different tunnels (and therefore different "NAT" settings)
     * happen to send traffic to the exact same destination (remote ip:port pair) then we will NOT
     * be able to accurately match any responses.
     * <p>
     * Also, if the TEID identifying the tunnel over which these messages are coming in (over S5/S8),
     * we'll calculate a new {@link NatEntry}, which means that, again, if two or more UE's send
     * traffic to the same remote endpoint, it won't work. However, for testing purposes we want this since
     * if you keep restarting the UE you want the new "PdnSession" to be remembered and not the old.
     * <p>
     * Again, this is a very simple SGi example so perhaps we'll do the above at some point. Also, we can
     * only do UDP (could use sockraw, a java wrapper for unit raw sockets but don't care for now and
     * didn't want to have that dependency since it is not published on a public maven repo)
     * <p>
     * Also note that we are going to be leaking these {@link NatEntry}s right now since we currently
     * do not "listen" to when the {@link GtpTunnel} goes down/is destroyed. But whatever, it's an example.
     * <p>
     *
     * @param tunnel
     * @param pdu
     */
    public void processPdu(final GtpTunnel tunnel, final PdnSessionContext session, final Gtp1Message pdu) {
        final var payload = pdu.getPayload().get();
        final var ipv4 = IpMessage.frame(payload).toIPv4();
        final var udp = UdpMessage.frame(ipv4.getPayload());
        final var remoteEndpoint = ConnectionEndpointId.create(Transport.udp, ipv4.getDestinationIp(), udp.getDestinationPort());

        final var teid = session.getLocalBearerTeid();

        final var natEntry = sessions.computeIfAbsent(teid, key -> {
            final var connection = connect(key.toString(), remoteEndpoint);
            final var deviceEndpoint = ConnectionEndpointId.create(Transport.udp, ipv4.getSourceIp(), udp.getSourcePort());
            final var nat = new NatEntry(session, tunnel, connection, deviceEndpoint, remoteEndpoint);
            nats.put(connection.id().getLocalConnectionEndpointId(), nat);
            return nat;
        });

        natEntry.externalConnection.send(udp.getPayload());
    }

    // TODO: allocate new local "NIC" for this mapping...
    public void onNewPdnSession(final GtpTunnel tunnel, final PdnSessionContext session) {

    }

    public void deleteSession(final PdnSessionContext session) {
        final var teid = session.getLocalBearerTeid();
        final var natEntry = sessions.remove(teid);
        if (natEntry != null) {
            final var localEndpoint = natEntry.externalConnection.id().getLocalConnectionEndpointId();
            nats.remove(localEndpoint);
            environment.getNetworkInterface(teid.toString()).ifPresent(NetworkInterface::down);
        } else {
            System.err.println("WTF - wrong TEID");
        }

    }

    private BufferConnection connect(final String nicName, final ConnectionEndpointId remote) {
        try {
            return environment.connect(nicName, Transport.udp, 0, remote.getAddress()).toCompletableFuture().get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException("We are cheating right now but seems like it didn't work", e);
        }
    }

    private void processBuffer(final BufferConnection connection, final BufferReadEvent msg) {
        final var connectionId = msg.getConnectionId();
        final var remote = connectionId.getRemoteConnectionEndpointId();
        final var local = connectionId.getLocalConnectionEndpointId();
        final var natEntry = nats.get(local);
        if (natEntry == null) {
            System.err.println("WTF - the NAT Entry is null. Guess we removed it before we processed the last data???");
        }

        final var content = msg.getBuffer();
        final var udp = UdpMessage.createUdpIPv4(content)
                .withSourcePort(remote.getPort())
                .withDestinationPort(natEntry.deviceEndpoint.getPort())
                .withSourceIp(remote.getIpAddress())
                .withDestinationIp(natEntry.deviceEndpoint.getIpAddress())
                .withTTL(64) // TODO: should probably grab this from the incoming UDP packet.
                .build();

        final var gtpU = ImmutableGtp1Message.create(Gtp1MessageType.G_PDU)
                .withTeid(natEntry.session.getLocalBearerTeid())
                .withPayload(udp.getBuffer())
                .build();
        natEntry.tunnel.send(gtpU);
    }

    private static class NatEntry {
        public final PdnSessionContext session;

        /**
         * The GTP-U tunnel that is "pointing" back to the actual device on the GRX network
         */
        public final GtpTunnel tunnel;

        /**
         * The "internet" connection pointing to the remote external party.
         * I.e., this is where the UE (user equipment - device - your cell phone if you will)
         * is sending (some) traffic)
         */
        public final BufferConnection externalConnection;

        public final ConnectionEndpointId deviceEndpoint;
        public final ConnectionEndpointId remoteEndpoint;

        private NatEntry(final PdnSessionContext session,
                         final GtpTunnel tunnel,
                         final BufferConnection externalConnection,
                         final ConnectionEndpointId deviceEndpoint,
                         final ConnectionEndpointId remoteEndpoint) {
            this.session = session;
            this.tunnel = tunnel;
            this.externalConnection = externalConnection;
            this.deviceEndpoint = deviceEndpoint;
            this.remoteEndpoint = remoteEndpoint;
        }
    }

}
