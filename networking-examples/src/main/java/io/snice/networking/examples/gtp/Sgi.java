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
import io.snice.networking.gtp.GtpTunnel;
import io.snice.networking.gtp.PdnSessionContext;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import static io.snice.networking.app.NetworkBootstrap.ACCEPT_ALL;

public class Sgi extends BufferApplication<SgiConfig> {

    private BufferEnvironment<SgiConfig> environment;
    // private final ConcurrentMap<ConnectionEndpointId, NatEntry> nats = new ConcurrentHashMap<>();

    private final ConcurrentMap<Teid, NatEntry> sessions = new ConcurrentHashMap<>();

    private final ConcurrentMap<ConnectionEndpointId, NatEntry> nats = new ConcurrentHashMap<>();

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
            final var connection = connect(remoteEndpoint);
            final var deviceEndpoint = ConnectionEndpointId.create(Transport.udp, ipv4.getSourceIp(), udp.getSourcePort());
            System.err.println("Got a new local connection for TEID " + teid + " -> " + connection.id().getLocalConnectionEndpointId());
            // TODO: also need to store this under this local key...
            final var nat = new NatEntry(session, tunnel, connection, deviceEndpoint, remoteEndpoint);
            nats.put(connection.id().getLocalConnectionEndpointId(), nat);
            return nat;
        });

        // NOTE NOTE NOTE: not thread safe. See comment on this method in general why this
        // over simplified example is just that, an example...
        /*
        if (!natEntry.session.getLocalBearerTeid().equals(teid)) {
            System.err.println("TEID changed: recalculating");
            nats.computeIfPresent(remoteEndpoint, (ep, currentEntry) -> {
                final var deviceEndpoint = ConnectionEndpointId.create(Transport.udp, ipv4.getSourceIp(), udp.getSourcePort());
                final var newEntry = new NatEntry(session, tunnel, natEntry.externalConnection, deviceEndpoint, ep);
                return newEntry;
            });
        }
         */

        natEntry.externalConnection.send(udp.getPayload());
    }

    private BufferConnection connect(final ConnectionEndpointId remote) {
        try {
            return environment.connect(Transport.udp, 0, remote.getAddress()).toCompletableFuture().get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException("We are cheating right now but seems like it didn't work", e);
        }
    }

    private void processBuffer(final BufferConnection connection, final BufferReadEvent msg) {
        final var connectionId = msg.getConnectionId();
        System.err.println("Got back a UDP message over local connection: " + connectionId.getLocalConnectionEndpointId());
        final var remote = connectionId.getRemoteConnectionEndpointId();
        final var local = connectionId.getLocalConnectionEndpointId();
        final var natEntry = nats.get(local);

        final var content = msg.getBuffer();
        final var udp = UdpMessage.createUdpIPv4(content)
                .withSourcePort(remote.getPort())
                .withDestinationPort(natEntry.deviceEndpoint.getPort())
                .withSourceIp(remote.getIpAddress())
                .withDestinationIp(natEntry.deviceEndpoint.getIpAddress())
                .withTTL(64) // TODO: should probably grab this from the incoming UDP packet.
                .build();

        System.err.println("using TEID: " + natEntry.session.getLocalBearerTeid());
        final var gtpU = ImmutableGtp1Message.create(Gtp1MessageType.G_PDU)
                .withTeid(natEntry.session.getLocalBearerTeid())
                .withPayload(udp.getBuffer())
                .build();
        natEntry.tunnel.send(gtpU);
    }

    private static class NatEntryKey {
        private final Teid teid;
        private final ConnectionEndpointId remoteEndpoint;

        private NatEntryKey(final Teid teid, final ConnectionEndpointId remoteEndpoint) {
            this.teid = teid;
            this.remoteEndpoint = remoteEndpoint;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final NatEntryKey that = (NatEntryKey) o;
            return teid.equals(that.teid) &&
                    remoteEndpoint.equals(that.remoteEndpoint);
        }

        @Override
        public int hashCode() {
            return Objects.hash(teid, remoteEndpoint);
        }
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
