package io.snice.networking.app.impl;

import io.netty.channel.ChannelHandlerContext;
import io.snice.networking.common.ChannelContext;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.event.IOEvent;
import io.snice.networking.common.event.MessageIOEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Whenever we invoke any type of user defined code, such as  state machine
 * which has been injected as part of the overall pipeline, we will actually
 * not allow that code to directly interact with the internals of
 * Snice Networking. E.g., if a user defined state machine wish to
 * send a message to the network and as such invokes {@link #sendDownstream(Object)}
 * we will cache that message and only actually send that message downstream
 * after the control has been regained by Snice Networking. I.e., as long
 * as the control is with the user code, we will cache all operations
 * from the application.
 *
 * @param <T>
 */
public class BufferingChannelContext<T> implements ChannelContext<T> {

    private final ConnectionId connectionId;
    private T downstream;
    private T upstream;
    private IOEvent<T> userEvent;


    /**
     * If the user wish to send multiple up or downstream messages then we'll
     * use these lists. Depending on the protocol and the state machines
     * that this may be powering, it may or may not be very common to have
     * a single invocation of e.g. a state machine yielding multiple up and
     * downstreams. This is e.g. true for a SIP Transaction who at most will
     * generate a single downstream and/or a single upstream for every invocation.
     * <p>
     * But Warning: early optimization is the root of all evil! :-)
     */
    private final List<T> downstreams = new ArrayList<>(4); // TODO: should be configurable
    private final List<T> upstreams = new ArrayList<>(4);
    private final List<IOEvent<T>> userEvents = new ArrayList<>(4);

    public BufferingChannelContext(final ConnectionId connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public ConnectionId getConnectionId() {
        return connectionId;
    }

    @Override
    public void sendDownstream(final T msg) {
        if (downstream == null) {
            downstream = msg;
        } else {
            downstreams.add(msg);
        }
    }

    @Override
    public void sendUpstream(final T msg) {
        if (upstream == null) {
            upstream = msg;
        } else {
            upstreams.add(msg);
        }
    }

    @Override
    public void fireUserEvent(IOEvent<T> evt) {
        if (userEvent == null) {
            userEvent = evt;
        } else {
            userEvents.add(evt);
        }
    }

    public void processDownstream(final ChannelHandlerContext ctx, final IOEvent<T> originalEvent) {
        if (downstream == null) {
            return;
        }

        ctx.write(downstream);
        downstream = null;

        downstreams.forEach(ctx::write);
        downstreams.clear();
    }

    public void processUpstream(final ChannelHandlerContext ctx, final IOEvent<T> originalEvent) {
        if (upstream == null) {
            return;
        }

        ctx.fireChannelRead(wrap(upstream, originalEvent));
        upstream = null;

        upstreams.forEach(msg -> ctx.fireChannelRead(wrap(msg, originalEvent)));
        upstreams.clear();
    }

    public void processEvents(final ChannelHandlerContext ctx, final IOEvent<T> originalEvent) {
        if (userEvent == null) {
            return;
        }

        ctx.fireUserEventTriggered(userEvent);
        userEvent = null;

        userEvents.forEach(ctx::fireUserEventTriggered);
        userEvents.clear();
    }

    /**
     * Whenever the FSM, or some other user code, decides to send an event
     * up or downstream, we'll take the time stamp and channel context
     * from the original event. The reason is that for e.g. the incoming
     * message has its time stamp set to the arrival time, i.e., the time
     * we successfully framed the message off of the network socket. We'd like
     * to preserve that arrival time all the way up to the client.
     *
     * @param msg
     * @param originalEvent
     * @return
     */
    private MessageIOEvent<T> wrap(final T msg, final IOEvent<T> originalEvent) {
        return MessageIOEvent.create(originalEvent.channelContext(), originalEvent.arrivalTime(), msg);
    }

}
