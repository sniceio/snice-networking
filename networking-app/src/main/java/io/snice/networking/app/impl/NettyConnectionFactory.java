package io.snice.networking.app.impl;

import io.netty.channel.ChannelHandlerContext;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

import java.util.Optional;

public class NettyConnectionFactory {

    Optional<Connection> accept(ConnectionId id, ChannelHandlerContext ctx) {
        return null;
    }
}
