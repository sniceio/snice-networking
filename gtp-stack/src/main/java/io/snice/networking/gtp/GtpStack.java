package io.snice.networking.gtp;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

public interface GtpStack<C extends GtpAppConfig> extends ProtocolBundle<Connection<GtpEvent>, GtpEvent, C> {


    C getConfig();

    void send(GtpMessageWriteEvent event);

    void send(GtpMessage msg, ConnectionId connection);

    void close(ConnectionId connection);

    CompletionStage<GtpControlTunnel> establishControlPlane(InetSocketAddress remoteAddress);

    CompletionStage<GtpUserTunnel> establishUserPlane(InetSocketAddress remoteAddress);

}
