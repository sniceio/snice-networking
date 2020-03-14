package io.snice.networking.diameter.peer;

import io.hektor.fsm.FSM;
import io.hektor.fsm.TransitionListener;
import io.snice.networking.codec.diameter.avp.api.HostIpAddress;
import io.snice.networking.codec.diameter.avp.type.IpAddress;
import io.snice.networking.common.ChannelContext;
import io.snice.networking.diameter.Peer;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;

import java.util.List;
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
     * A {@link Peer} must have a local IP associated with it.
     */
    protected final HostIpAddress LOCAL_PEER_IP_ADDRESS = HostIpAddress.of(IpAddress.createIpv4Address("10.36.10.10"));

    protected PeerConfiguration peerConfiguration;

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
        peerConfiguration = new PeerConfiguration();
        peerConfiguration.setProductName(defaultProductName);

        when(ctx.getChannelContext()).thenReturn(channelCtx);
        when(ctx.getHostIpAddresses()).thenReturn(List.of(LOCAL_PEER_IP_ADDRESS));
        when(ctx.getConfig()).thenReturn(peerConfiguration);

        fsm = PeerFsm.definition.newInstance(UUID.randomUUID(), ctx, data, unhandledEventHandler, transitionListener);
        fsm.start();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}