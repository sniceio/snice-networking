package io.snice.networking.app.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.snice.buffer.Buffer;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.codec.Framer;
import io.snice.networking.codec.SerializationFactory;
import io.snice.networking.codec.diameter.DiameterHeader;
import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.impl.DiameterParser;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.netty.TcpConnection;
import io.snice.time.Clock;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * NOTE: this class is not sharable. If you do not know what that means, read up on
 * the Netty ChannelHandlers and @Sharable (the netty annotation). In short, each
 * channel will have it's own handler because it stores states that is unique
 * to only that channel and as such, it cannot be shared...
 *
 */
public class NettyTcpInboundAdapter<T> implements ChannelInboundHandler {

    private final Clock clock;
    private final Optional<URI> vipAddress;
    private final UUID uuid = UUID.randomUUID();
    private final List<ConnectionContext> ctxs;
    private final SerializationFactory<T> factory;
    private int exceptionCounter = 0;

    private DiameterHeader currentHeader;
    private DiameterMessage lastMessage;

    private final ReentrantLock lock = new ReentrantLock();


    /**
     * If the incoming connection doesn't match anything, then we'll use this
     * default context instead.
     */
    private final ConnectionContext defaultCtx;

    /**
     * Represents the underlying channel and for TCP, we will always have
     * a local and remote peer since this is after all a connection oriented
     * protocol.
     */
    private ConnectionAdapter<TcpConnection, T> connection;

    public NettyTcpInboundAdapter(final Clock clock, final SerializationFactory<T> factory, final Optional<URI> vipAddress, final List<ConnectionContext> ctxs) {
        this.clock = clock;
        this.factory = factory;
        this.vipAddress = vipAddress;
        this.ctxs = ctxs;

        // TODO
        defaultCtx = null;
    }

    private void log(final String msg) {
        System.out.println("[ " + uuid + " TCP ]: " + msg);
    }

    private void logError(final String msg) {
        System.err.println("[ " + uuid + " TCP ]: " + msg);
    }

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        /*
        log("channel registered");
        if (ctx.channel().localAddress() != null) {
            ctx.fireUserEventTriggered(create(ctx, ConnectionOpenedIOEvent::create));
        }
         */
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        /*
        log("channel unregistered");
        if (ctx.channel().localAddress() != null) {
            ctx.fireUserEventTriggered(create(ctx, ConnectionClosedIOEvent::create));
        }
         */
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        if (connection != null) {
            System.err.println("WTF - you can't have yet another connection coming in???");
        }

        final var channel = ctx.channel();
        final var localAddress = (InetSocketAddress)channel.localAddress();
        final var remoteAddress = (InetSocketAddress)channel.remoteAddress();
        final var id = ConnectionId.create(Transport.tcp, localAddress, remoteAddress);
        final var connCtx = findContext(id);

        if (connCtx.isDrop()) {
            ctx.close();
            return;
        }

        final Framer<T> framer = factory.getFramer();
        connection = new ConnectionAdapter(new TcpConnection(channel, id, vipAddress), framer, connCtx);
    }

    private ConnectionContext<Connection, T> findContext(final ConnectionId id) {
        return ctxs.stream().filter(ctx -> ctx.test(id)).findFirst().orElse(defaultCtx);
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        log("channel inactive");
        if (ctx.channel().localAddress() != null) {
            // final ConnectionIOEvent event = create(ctx, ConnectionInactiveIOEvent::create);
            // ctx.fireUserEventTriggered(event);
        }
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final T t = (T)msg;
        connection.process(t);
    }

    public void channelReadOld(final ChannelHandlerContext ctx, final Object msg) throws Exception {

        lock.lock();
        try {
            final var bytebuf = (ByteBuf) msg;
            if (currentHeader == null) {
                if (bytebuf.readableBytes() < 20) {
                    return;
                }

                final byte[] headerBytes = new byte[20];
                bytebuf.readBytes(headerBytes);
                final Buffer headerBuffer = Buffer.of(headerBytes);
                currentHeader = DiameterHeader.frame(headerBuffer.toReadableBuffer());
            }

            // because the total length includes the header, which we've just read.
            final int length = currentHeader.getLength() - 20;
            if (bytebuf.readableBytes() >= length) {
                final int currentReaderIndex = bytebuf.readerIndex();
                final byte[] avp = new byte[length];
                bytebuf.readBytes(avp);
                final int totalBytesRead = bytebuf.readerIndex() - currentReaderIndex;
                if (totalBytesRead != length) {
                    logError("We only read " + totalBytesRead);
                }
                bytebuf.discardReadBytes();
                final Buffer avpBuffer = Buffer.of(avp);
                final DiameterHeader header = currentHeader;
                currentHeader = null;
                try {

                    final DiameterMessage diameter = DiameterParser.frame(header, avpBuffer.toReadableBuffer());
                    lastMessage = diameter;
                    connection.process((T) diameter);
                } catch (final IndexOutOfBoundsException e) {
                    ++exceptionCounter;
                    final DiameterHeader error = currentHeader;
                    logError("Exception no " + exceptionCounter);
                    logError("Current Header: " + header);
                    logError(header.getBuffer().dumpAsHex());
                    logError("Current Buffer: ");
                    logError(avpBuffer.dumpAsHex());

                    logError("Last Header: " + lastMessage.getHeader().getBuffer().toString());
                    logError(lastMessage.getHeader().getBuffer().dumpAsHex());
                    logError("Last Msg AVPS: " + lastMessage.getAllAvps().size());
                    logError(lastMessage.getBuffer().dumpAsHex());
                    e.printStackTrace();
                    ctx.channel().close().sync();
                    currentHeader = null;
                    System.exit(1);
                    throw e;
                }
                // ctx.fireChannelRead(diameter);
            }
        } finally {
            lock.unlock();
        }


        /*
        while (buffer.isReadable()) {
            final int availableBytes = buffer.readableBytes();
            final int toWrite = Math.min(availableBytes, availableBytes);
            final byte[] data = new byte[toWrite];
            buffer.readBytes(data);
            connection.frame(Buffers.wrap(data)).ifPresent(connection::process);
        }
         */
    }

    /**
     * From ChannelInboundHandler
     */
    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext ctx) throws Exception {
        // just consume the event
        log("writability changed");
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        log("handler added " + ctx.name());

    }

    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
        log("handler removed : " + ctx.name());

    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        log("exception " + cause.getMessage());
        cause.printStackTrace();
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        log("user event");
    }

}
