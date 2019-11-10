package io.snice.networking.app.impl;

import io.hektor.fsm.Data;
import io.hektor.fsm.FSM;
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
     * also be equal to a single FSM per TCP connection. However, it is not true for UDP where the
     * same pipeline is used for all and as such, it will have to work a little harder...
     */
    private FSM<S, C, D> fsm;
    private final FsmFactory<T, S, C, D> fsmFactory;


    public NettyFsmLayer(final FsmFactory<T, S, C, D> fsmFactory) {
        this.fsmFactory = fsmFactory;
    }


    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object object) throws Exception {
        final var event = (MessageIOEvent<T>) object;
        final var fsm = ensureFSM(event);
        invokeFSM(fsm, event.getMessage());

        // ctx.fireChannelRead(event);
    }

    private void invokeFSM(final FSM<S, C, D> fsm, final Object event) {
        // TODO: we are going to use a buffered ChannelContext etc like always
        fsm.onEvent(event);
    }


    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        final var ioEvent = (IOEvent<T>) evt;
        logger.info("Yeah, user event triggered: " + ioEvent);
        ctx.fireUserEventTriggered(evt);
    }


    private FSM<S, C, D> ensureFSM(final MessageIOEvent<T> msgEvent) {
        if (fsm != null) {
            return fsm;
        }

        final var msg = msgEvent.getMessage();
        final var channelCtx = msgEvent.channelContext();
        final var connectionId = channelCtx.getConnectionId();

        // TODO: we need to wrap the channel context in something so the
        // FSM just can't send out to the network before we are back in
        // control

        final var fsmKey = fsmFactory.calculateKey(connectionId, Optional.of(msg));
        final var ctx = fsmFactory.createNewContext(fsmKey, channelCtx);
        final var data = fsmFactory.createNewDataBag(fsmKey);
        fsm = fsmFactory.createNewFsm(fsmKey, ctx, data);
        fsm.start();
        return fsm;
    }

}
