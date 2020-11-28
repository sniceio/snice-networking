package io.snice.networking.gtp;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.codecs.codec.gtp.gtpc.v2.Impl.Gtp2MessageBuilder;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.gtp.conf.ControlPlaneConfig;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

public interface GtpStack<C extends GtpAppConfig> extends ProtocolBundle<Connection<GtpEvent>, GtpEvent, C> {


    C getConfig();

    void send(GtpMessageWriteEvent event);

    /**
     * Ask the {@link GtpStack} to send the given message over the connection identified by the
     * {@link ConnectionId}.
     *
     * @param msg
     * @param connection
     */
    void send(GtpMessage msg, ConnectionId connection);

    void close(ConnectionId connection);

    /**
     * Attempt to establish a {@link GtpControlTunnel} to the given remote address. The local
     * network interface that will be used for this tunnel is based on the name in the
     * {@link ControlPlaneConfig#getNic()}.
     * <p>
     * TODO:
     *
     * @param remoteAddress the remote address to connect to.
     * @return a {@link CompletionStage} that, if completes successfully, will return a {@link GtpControlTunnel}.
     */
    CompletionStage<GtpControlTunnel> establishControlPlane(InetSocketAddress remoteAddress);

    CompletionStage<GtpUserTunnel> establishUserPlane(InetSocketAddress remoteAddress);

    /**
     * To create a new stack maintained 4G PDN Session, ask the {@link GtpStack} to initiate one.
     * The given {@link Gtp2Request} must be a Create Session Request but apart from that, no
     * other rules are enforced. If you wish the {@link GtpStack} to "fill out" more information
     * for you, then use the overloaded {@link #initiateNewPdnSession(Gtp2MessageBuilder)} instead.
     *
     * @param createSessionRequest the Create Session Request.
     * @return a new {@link PdnSession.Builder} that allows you to register callbacks etc.
     */
    PdnSession.Builder initiateNewPdnSession(Gtp2Request createSessionRequest);

    /**
     * To create a new stack maintained 4G PDN Session, ask the {@link GtpStack} to initiate one.
     * The given {@link Gtp2MessageBuilder} must be a Create Session Request and the stack will
     * "fill out" default mandatory TLIVs unless already set by you.
     * <p>
     * The information that will be filled out by the stack is:
     * <p>
     * TODO: document it
     * <ul>
     *     <li></li>
     *     <li></li>
     *     <li></li>
     *     <li></li>
     *     <li></li>
     * </ul>
     *
     * @param createSessionRequest the Create Session Request builder.
     * @return a new {@link PdnSession.Builder} that allows you to register callbacks etc.
     */
    PdnSession.Builder initiateNewPdnSession(Gtp2MessageBuilder<Gtp2Request> createSessionRequest);

}
