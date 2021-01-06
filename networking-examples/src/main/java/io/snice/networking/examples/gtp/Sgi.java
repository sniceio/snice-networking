package io.snice.networking.examples.gtp;

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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import static io.snice.networking.app.NetworkBootstrap.ACCEPT_ALL;

public class Sgi extends BufferApplication<SgiConfig> {

    private BufferEnvironment<SgiConfig> environment;
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
     * happen to send traffic to the exact same destination (remote ip:port pair) then we will not
     * be able to accurately match any responses.
     * <p>
     * Again, this is a very simple SGi example so perhaps we'll do the above at some point. Also, we can
     * only do UDP (could use sockraw, a java wrapper for unit raw sockets but don't care for now and
     * didn't want to have that dependency since it is not published on a public maven repo)
     * <p>
     * Also note that we are going to be leaking these {@link NatEntry}s right now since we currently
     * do not "listen" to when the {@link GtpTunnel} goes down/is destroyed. But whatever, it's an example.
     * <p>
     * Rocksaw:
     * https://www.savarese.com/software/rocksaw/
     * https://github.com/mlaccetti/rocksaw
     *
     * @param tunnel
     * @param pdu
     */
    public void processPdu(final GtpTunnel tunnel, final PdnSessionContext session, final Gtp1Message pdu) {
        final var payload = pdu.getPayload().get();
        final var ipv4 = IpMessage.frame(payload).toIPv4();
        final var udp = UdpMessage.frame(ipv4.getPayload());
        final var remoteEndpoint = ConnectionEndpointId.create(Transport.udp, ipv4.getDestinationIp(), udp.getDestinationPort());

        final var natEntry = nats.computeIfAbsent(remoteEndpoint, ep -> {
            // NOTE: this is BAD in general but since we know that right now we can only do
            // UDP and that completes right away, we are cheating here... In a real "SGi"
            // implementation you really cannot do this since if it e.g. is TCP, this may not
            // completely in a timely fashion, or at all.
            System.err.println("New NAT Entry for endpoint: " + remoteEndpoint);
            final var connection = connect(ep);
            final var deviceEndpoint = ConnectionEndpointId.create(Transport.udp, ipv4.getSourceIp(), udp.getSourcePort());
            return new NatEntry(session, tunnel, connection, deviceEndpoint, ep);
        });

        natEntry.externalConnection.send(udp.getPayload());
    }

    private BufferConnection connect(final ConnectionEndpointId remote) {
        try {
            return environment.connect(remote).toCompletableFuture().get();
        } catch (final InterruptedException | ExecutionException e) {
            throw new RuntimeException("We are cheating right now but seems like it didn't work", e);
        }
    }


    private void processBuffer(final BufferConnection connection, final BufferReadEvent msg) {
        final var remote = msg.getConnectionId().getRemoteConnectionEndpointId();
        final var natEntry = nats.get(remote);

        final var content = msg.getBuffer();
        final var udp = UdpMessage.createUdpIPv4(content)
                .withSourcePort(remote.getPort())
                .withDestinationPort(natEntry.deviceEndpoint.getPort())
                .withSourceIp(remote.getIpAddress())
                .withDestinationIp(natEntry.deviceEndpoint.getIpAddress())
                .withTTL(64) // TODO: should probably grab this from the incoming UDP packet.
                .build();

        final var gtpU = ImmutableGtp1Message.create(Gtp1MessageType.G_PDU)
                .withTeid(natEntry.session.getDefaultRemoteBearer().getTeid())
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
