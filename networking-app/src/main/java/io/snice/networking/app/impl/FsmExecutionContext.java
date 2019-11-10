package io.snice.networking.app.impl;

import io.hektor.fsm.Data;
import io.hektor.fsm.FSM;
import io.netty.channel.ChannelHandlerContext;
import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.event.MessageIOEvent;
import io.snice.networking.common.fsm.NetworkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jonas@jonasborjesson.com
 */
public class FsmExecutionContext<T, S extends Enum<S>, C extends NetworkContext<T>, D extends Data> {

    private static final Logger logger = LoggerFactory.getLogger(FsmExecutionContext.class);

    private final T initialMsg;
    private final BufferingChannelContext<T> ctx;
    private final FSM<S, C, D> fsm;
    private final ChannelHandlerContext nettyCtx;

    public FsmExecutionContext(final T initialMsg,
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

    public void onUpstreamMessage(final MessageIOEvent<T> event) {
        invokeFSM(event);
    }


    public void onDownstreamMessage(final MessageIOEvent<T> msg) {
        logger.warn("need to take care of the downstream message: {} ", msg);
    }

    private void invokeFSM(final MessageIOEvent<T> event) {
        fsm.onEvent(event.getMessage());
        ctx.processDownstream(nettyCtx, event);
        ctx.processUpstream(nettyCtx, event);
    }

}
