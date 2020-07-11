package io.snice.networking.app.impl;

import io.hektor.fsm.Data;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.snice.networking.common.event.IOEvent;
import io.snice.networking.common.event.MessageIOEvent;
import io.snice.networking.common.fsm.FsmFactory;
import io.snice.networking.common.fsm.NetworkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * <p>
 * An optional handler in the Netty pipeline that provides an execution environment
 * for FSMs. In the Snice Networking model, the user application can insert a
 * FSM into the pipeline and control "raw" events before reaching the actual
 * application. This is somewhat common in various protocols such as:
 * </p>
 *
 * <p>
 * Note that the thread model for this handler assumes it is executing
 * within a single netty pipeline, which guarantees that only a single
 * thread is handling it. As such, there is no locking in this class but also
 * note that this means that this class is NOT THREAD SAFE! So if you were
 * to use this class in some other context, you really need to write another
 * one... TODO: create some base class for thread safe vs non-thread safe.
 * </p>
 *
 * <p>
 * SIP - the SIP transaction layer sits between the low-level networking layer and the
 * so-called TransactionUser layer and is responsible for dealing with re-transmissions
 * and more.
 * </p>
 *
 * <p>
 * Diameter - in Diameter you have a so-called Peer which is somewhat similar to that of
 * a SIP Transaction in that it is also responsible for dealing with re-transmissions
 * etc but it is also deals with managing the connection with the remote entity (in SIP
 * you don't quite have that in the same sense, there is another RFC for that in SIP
 * that deals with so-called Flows (RFC5626)
 * </p>
 */
public class NettyFsmLayer<T, S extends Enum<S>, C extends NetworkContext<T>, D extends Data> extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyFsmLayer.class);

    /**
     * This particular netty fsm layer is for those types that always just has a single
     * FSM per netty pipeline. For connection oriented protocols, such as TCP, that would then
     * also be equal to a single FSM per TCP connection.
     */
    private FsmExecutionContext<T, S, C, D> fsmExecutionContext;
    private final FsmFactory<T, S, C, D> fsmFactory;


    public NettyFsmLayer(final FsmFactory<T, S, C, D> fsmFactory) {
        this.fsmFactory = fsmFactory;
    }


    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object object) throws Exception {
        final var event = (MessageIOEvent<T>) object;
        ensureExecutionContext(event, ctx).onUpstreamMessage(event);
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        logger.info("UserEventTriggered: " + evt);
        final var ioEvent = (IOEvent<T>) evt;
        ensureExecutionContext(ioEvent, ctx).onUpstreamMessage(ioEvent);

        // Note: keeping this one as a comment to point out the following.
        // It should not be called because the actual state machine needs to
        // decide if this is an event that should be propagated. As such, you
        // cannot just blindly call this one below. If you did, you would propagate
        // events around/outside the state machine, which may or may not wanted this
        // event to be propagated up the chain.
        // ctx.fireUserEventTriggered(evt);
    }


    private FsmExecutionContext<T, S, C, D> ensureExecutionContext(final IOEvent<T> event,
                                                                   final ChannelHandlerContext nettyCtx) {
        if (fsmExecutionContext != null) {
            return fsmExecutionContext;
        }

        final var channelCtx = event.channelContext();
        final var connectionId = channelCtx.getConnectionId();
        final var bufferingCtx = new BufferingChannelContext<T>(connectionId);

        final Optional<T> optionalMsg = event.isMessageIOEvent() ? Optional.of(event.toMessageIOEvent().getMessage()) : Optional.empty();
        final var fsmKey = fsmFactory.calculateKey(connectionId, optionalMsg);
        final var ctx = fsmFactory.createNewContext(fsmKey, bufferingCtx);
        final var data = fsmFactory.createNewDataBag(fsmKey);
        final var fsm = fsmFactory.createNewFsm(fsmKey, ctx, data);

        fsmExecutionContext = new FsmExecutionContext<>(event, bufferingCtx, nettyCtx, fsm);
        fsmExecutionContext.start();
        return fsmExecutionContext;
    }

}
