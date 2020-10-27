package io.snice.networking.app.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

import static io.snice.preconditions.PreConditions.assertNotNull;

public interface UdpReadEvent<T> {

    static <T> UdpReadEvent<T> create(final ChannelHandlerContext ctx, final DatagramPacket raw, final T message) {
        assertNotNull(ctx);
        assertNotNull(raw);
        assertNotNull(message);
        return new DefaultUdpReadEvent<>(ctx, raw, message);
    }

    ChannelHandlerContext getCtx();

    DatagramPacket getRaw();

    T getMessage();

    class DefaultUdpReadEvent<T> implements UdpReadEvent<T> {
        private final ChannelHandlerContext ctx;
        private final DatagramPacket raw;
        private final T message;

        private DefaultUdpReadEvent(final ChannelHandlerContext ctx, final DatagramPacket raw, final T message) {
            this.ctx = ctx;
            this.raw = raw;
            this.message = message;
        }

        @Override
        public ChannelHandlerContext getCtx() {
            return ctx;
        }

        @Override
        public DatagramPacket getRaw() {
            return raw;
        }

        @Override
        public T getMessage() {
            return message;
        }
    }

}
