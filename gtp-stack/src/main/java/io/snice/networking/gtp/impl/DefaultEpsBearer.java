package io.snice.networking.gtp.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1MessageType;
import io.snice.codecs.codec.gtp.gtpc.v1.impl.ImmutableGtp1Message;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Paa;
import io.snice.codecs.codec.transport.UdpMessage;
import io.snice.networking.gtp.Bearer;
import io.snice.networking.gtp.EpsBearer;
import io.snice.networking.gtp.GtpUserTunnel;
import io.snice.networking.gtp.PdnSessionContext;
import io.snice.preconditions.PreConditions;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultEpsBearer implements EpsBearer {
    private final GtpUserTunnel tunnel;
    private final Paa paa;
    private final Bearer localBearer;
    private final Bearer remoteBearer;
    private final int defaultLocalPort;

    public static EpsBearer create(final GtpUserTunnel tunnel, final PdnSessionContext ctx, final int defaultLocalPort) {
        assertNotNull(tunnel, "The GTP User Tunnel cannot be null");
        assertNotNull(ctx, "The PDN Session Context cannot be null");
        final var remoteBearer = ctx.getDefaultRemoteBearer();
        return new DefaultEpsBearer(tunnel, ctx.getPaa(), ctx.getDefaultLocalBearer(), remoteBearer, defaultLocalPort);
    }

    private DefaultEpsBearer(final GtpUserTunnel tunnel, final Paa paa, final Bearer localBearer, final Bearer remoteBearer, final int defaultLocalPort) {
        this.tunnel = tunnel;
        this.paa = paa;
        this.localBearer = localBearer;
        this.remoteBearer = remoteBearer;
        this.defaultLocalPort = defaultLocalPort;
    }

    @Override
    public void send(final String remoteAddress, final int remotePort, final Buffer data) {
        final var ipv4 = UdpMessage.createUdpIPv4(data)
                .withDestinationPort(remotePort)
                .withSourcePort(defaultLocalPort)
                .withTTL(64)
                .withDestinationIp(remoteAddress)
                .withSourceIp(paa.getValue().getIPv4Address().get())
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
