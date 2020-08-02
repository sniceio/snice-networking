package io.snice.networking.app.impl;

import io.hektor.fsm.Data;
import io.hektor.fsm.FSM;
import io.netty.channel.ChannelHandlerContext;
import io.snice.networking.common.event.IOEvent;
import io.snice.networking.common.fsm.NetworkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jonas@jonasborjesson.com
 */
public class FsmExecutionContext<T, S extends Enum<S>, C extends NetworkContext<T>, D extends Data> {

    private static final Logger logger = LoggerFactory.getLogger(FsmExecutionContext.class);

    private final IOEvent<T> initialMsg;
    private final BufferingChannelContext<T> ctx;
    private final FSM<S, C, D> fsm;
    private final ChannelHandlerContext nettyCtx;

    public FsmExecutionContext(final IOEvent<T> initialMsg,
                               final BufferingChannelContext<T> ctx,
                               final ChannelHandlerContext nettyCtx,
                               final FSM<S, C, D> fsm) {
        this.initialMsg = initialMsg;
        this.ctx = ctx;
        this.nettyCtx = nettyCtx;
        this.fsm = fsm;
    }

    public void start() {
        fsm.start();
    }

    public void onUpstreamMessage(final IOEvent<T> event) {
        invokeFSM(event);
    }


    public void onDownstreamMessage(final IOEvent<T> msg) {
        logger.warn("need to take care of the downstream message: {} ", msg);
        throw new RuntimeException("Not implemented yet");
    }

    private void invokeFSM(final IOEvent<T> event) {
        if (event.isMessageIOEvent()) {
            final var msg = event.toMessageIOEvent();
            fsm.onEvent(msg.getMessage());
        } else {
            fsm.onEvent(event);
        }
        ctx.processDownstream(nettyCtx, event);
        ctx.processEvents(nettyCtx, event);
        ctx.processUpstream(nettyCtx, event);
    }

}
