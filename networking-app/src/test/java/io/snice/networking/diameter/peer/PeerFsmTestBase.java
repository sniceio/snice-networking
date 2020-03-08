package io.snice.networking.diameter.peer;

import io.hektor.fsm.FSM;
import io.hektor.fsm.TransitionListener;
import io.snice.networking.common.ChannelContext;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;

import java.util.UUID;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.when;

public class PeerFsmTestBase extends DiameterTestBase {

    @Mock
    protected PeerContext ctx;

    @Mock
    protected ChannelContext channelCtx;

    @Mock
    protected TransitionListener<PeerState> transitionListener;

    /**
     * Unless we are testing unhandled events specifically, this one
     * should never be called.
     */
    @Mock
    protected BiConsumer<PeerState, Object> unhandledEventHandler;

    protected PeerData data;

    protected FSM fsm;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(ctx.getChannelContext()).thenReturn(channelCtx);

        fsm = PeerFsm.definition.newInstance(UUID.randomUUID(), ctx, data, unhandledEventHandler, transitionListener);
        fsm.start();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}