package io.snice.networking.gtp.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1MessageType;
import io.snice.codecs.codec.gtp.gtpc.v1.impl.ImmutableGtp1Message;
import io.snice.codecs.codec.gtp.gtpc.v2.type.PdnType;
import io.snice.codecs.codec.transport.UdpMessage;
import io.snice.networking.gtp.Bearer;
import io.snice.networking.gtp.DataTunnel;
import io.snice.networking.gtp.EpsBearer;
import io.snice.networking.gtp.PdnSessionContext;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertNotEmpty;
import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultEpsBearer implements EpsBearer {

    private final InternalGtpUserTunnel tunnel;
    private final Buffer assignedDeviceIp;
    private final Bearer localBearer;
    private final Bearer remoteBearer;
    private final int defaultLocalPort;

    public static EpsBearer create(final InternalGtpUserTunnel tunnel, final PdnSessionContext ctx, final int defaultLocalPort) {
        assertNotNull(tunnel, "The GTP User Tunnel cannot be null");
        assertNotNull(ctx, "The PDN Session Context cannot be null");
        final var remoteBearer = ctx.getDefaultRemoteBearer();
        final var paa = ctx.getPaa().getValue();
        if (paa.getPdnType().getType() == PdnType.Type.IPv6) {
            throw new IllegalArgumentException("Sorry, can only do IPv4 addresses right now");
        }

        final var assignedIpAddress = paa.getIPv4Address().get();
        return new DefaultEpsBearer(tunnel, assignedIpAddress, ctx.getDefaultLocalBearer(), remoteBearer, defaultLocalPort);
    }

    public static EpsBearer create(final InternalGtpUserTunnel tunnel, final Buffer assignedDeviceIp, final Bearer localBearer, final Bearer remoteBearer, final int defaultLocalPort) {
        assertNotNull(tunnel, "The GTP User Tunnel cannot be null");
        Buffers.assertNotEmpty(assignedDeviceIp, "The assigned IP Address to the device cannot be null or the empty buffer");
        assertNotNull(localBearer, "The local Bearer cannot be null");
        assertNotNull(remoteBearer, "The remote Bearer cannot be null");

        return new DefaultEpsBearer(tunnel, assignedDeviceIp, localBearer, remoteBearer, defaultLocalPort);
    }

    private DefaultEpsBearer(final InternalGtpUserTunnel tunnel, final Buffer assignedDeviceIp, final Bearer localBearer, final Bearer remoteBearer, final int defaultLocalPort) {
        this.tunnel = tunnel;
        this.assignedDeviceIp = assignedDeviceIp;
        this.localBearer = localBearer;
        this.remoteBearer = remoteBearer;
        this.defaultLocalPort = defaultLocalPort;
    }

    @Override
    public <T> DataTunnel.Builder<T> createDataTunnel(final Class<T> type, final String remoteHost, final int port) {
        assertNotNull(type, "The given data type this tunnel will be sending/receiving cannot be null");
        assertNotEmpty(remoteHost, "You must specify the remote host");
        assertArgument(port >= 0, "The given port must be equal to or greater than zero");
        return tunnel.createDataTunnel(type, remoteHost, port)
                .withLocalTeid(localBearer.getTeid())
                .withRemoteTeid(remoteBearer.getTeid())
                .withLocalIPv4DeviceIp(assignedDeviceIp)
                .withLocalPort(defaultLocalPort);
    }

    @Override
    public Bearer getLocalBearer() {
        return localBearer;
    }

    @Override
    public Bearer getRemoteBearer() {
        return remoteBearer;
    }

    @Override
    public void send(final String remoteAddress, final int remotePort, final Buffer data) {
        final var ipv4 = UdpMessage.createUdpIPv4(data)
                .withDestinationPort(remotePort)
                .withSourcePort(defaultLocalPort)
                .withTTL(64)
                .withDestinationIp(remoteAddress)
                .withSourceIp(assignedDeviceIp)
                .build();

        final var gtpU = ImmutableGtp1Message.create(Gtp1MessageType.G_PDU)
                .withTeid(remoteBearer.getTeid())
                .withPayload(ipv4.getBuffer())
                .build();
        tunnel.send(gtpU);
    }

    @Override
    public void send(final String remoteAddress, final int remotePort, final String data) {
        send(remoteAddress, remotePort, Buffers.wrap(data));
    }
}
