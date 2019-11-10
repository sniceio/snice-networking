package io.snice.networking.common.fsm;

import io.hektor.fsm.Context;
import io.snice.networking.common.ChannelContext;

/**
 * Snice Networking provides an execution environment for those protocols that wish to
 * include an FSM as part of the networking pipeline. When e.g. a message is read from
 * the network, it will be passed up the chain of handlers. One of those handlers, if
 * configured, is a handler that provides an execution environment for the state machines
 * and upon receiving a message from the network, that message is then given to a
 * state machine. Depending on the protocol, that state machine may decide to either:
 *
 * <ol>
 * <li>Reply back itself, which is a common way for certain protocol handlers to handle
 * re-transmissions without involving the higher level application.</li>
 * <li>Allow the message to be propagated further up the chain of handlers, which then eventually
 * will be delivered to the actual application (unless some other handler dropped it along the way)</li>
 * <li>Or simply just silently drop the message on the floor.</li>
 * </ol>
 * <p>
 * In order for the Snice Networking layer to provide this common execution environment, the
 * FSM must implement this {@link NetworkContext}, which provides the three methods
 * described above. This is how Snice Networking knows what to do after the FSM
 * has been invoked.
 *
 * TODO: we are killing this one and will be using ChannelContext isntead.
 */
public interface NetworkContext<T> extends Context {

    ChannelContext<T> getChannelContext();

}
