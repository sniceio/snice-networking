package io.snice.networking.gtp;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.networking.app.ConnectionContext;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.app.impl.GenericBootstrap;
import io.snice.networking.app.impl.NettyApplicationLayer;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.core.NetworkInterface;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageReadEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;
import io.snice.networking.gtp.impl.DefaultGtpStack;
import io.snice.networking.gtp.impl.InternalGtpUserTunnel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Most of the actual functionality is provided by the {@link GtpStack} so the tests within are the
 * more important ones.
 */
public class GtpStackTest extends GtpStackTestBase {

    private GtpEnvironment<TestConfig> environment;

    @Captor
    ArgumentCaptor<GtpMessageWriteEvent> writeEventCaptor;

    @Mock
    private NetworkStack<Connection<GtpEvent>, GtpEvent, TestConfig> network;

    @Mock
    private NetworkInterface<GtpEvent> gtpuNic;

    @Mock
    private NetworkInterface<GtpEvent> gtpcNic;

    private DefaultGtpStack<TestConfig> stack;

    private GenericBootstrap<GtpTunnel, GtpEvent, TestConfig> defaultBootstrap;

    private TestConfig defaultConfig;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        defaultConfig = loadConfig(TestConfig.class, "gtp_config_001.yml");
        defaultBootstrap = new GenericBootstrap<>(defaultConfig);
        stack = new DefaultGtpStack<TestConfig>();

        // Default GTP app that just accepts all connections and all type of events.
        // This is good enough for e.g. testing the GtpStack's transaction handling
        // etc but if you want some other rules, create another test app and re-initialize
        // the stack in your unit test.
        final var app = new GtpTestApplication(bootstrap -> {
            bootstrap.onConnection(id -> true).accept(builder -> {
                builder.match(evt -> true).consume(evt -> System.out.println(evt));
            });
        });
        environment = initializeStack(stack, app, defaultBootstrap, defaultConfig);
    }

    /**
     * Test to initialize the {@link GtpStack}. Note that the stack is a {@link ProtocolBundle} and
     * is also being bootstrapped as an application, which the glue layer {@link GtpApplication} is
     * doing but we'll skip that one here and call the {@link DefaultGtpStack#initializeApplication(GtpApplication, NetworkBootstrap)}
     * directly.
     */
    @Test
    public void testInitializeGtpStack() {
        assertThat(environment, notNullValue());
    }


    @Test
    public void testEstablishUserTunnel() {
        final var connection = mockGtpUserConnect("127.0.0.1", 1111);
        final var tunnel = environment.establishUserPlane("127.0.0.1", 1111);
        assertThat(tunnel, notNullValue());

        // the gtp-u tunnels are always considered "connected"
        assertThat(tunnel.connect(), is(true));

        final var pdu = somePdu();
        tunnel.send(pdu);

        // this will ultimately be sent across the "real" connection, which we mocked
        // in the beginning of this stack so ensure it was actually "sent" across this connection
        verify(connection).send(writeEventCaptor.capture());
        final var event = writeEventCaptor.getValue();
        assertThat(event.getConnectionId(), is(tunnel.id()));
        assertThat(event.getMessage(), is(pdu));
    }

    /**
     * Of course, we cannot create a {@link DataTunnel} using a {@link GtpUserTunnel}
     * that doesn't exist/hasn't established.
     */
    @Test
    public void testCreateDataTunnelBadTunnel1() {
        // TODO: really need to start using parameterized test and time to swap to jUnit 5
        final var tunnel = mock(InternalGtpUserTunnel.class);
        when(tunnel.id()).thenReturn(defaultConnectionId);

        // Tunnel is unknown to the stack because it was never established.
        ensureBadTunnel(tunnel, String.class, defaultRemoteAddress, defaultRemotePort);

        ensureBadTunnel(null, String.class, defaultRemoteAddress, defaultRemotePort);
        ensureBadTunnel(tunnel, null, defaultRemoteAddress, defaultRemotePort);
        ensureBadTunnel(tunnel, String.class, "", defaultRemotePort);
        ensureBadTunnel(tunnel, String.class, null, defaultRemotePort);
        ensureBadTunnel(tunnel, String.class, defaultRemoteAddress, -1);
        ensureBadTunnel(tunnel, String.class, defaultRemoteAddress, 0);

    }

    private void ensureBadTunnel(final InternalGtpUserTunnel tunnel, final Class type, final String remoteAddress, final int port) {
        try {
            stack.createDataTunnel(tunnel, type, remoteAddress, port);
            Assert.fail("Expected the creation of the data tunnel to fail");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testCreateDataTunnel() throws Exception {
        final var connection = mockGtpUserConnect("127.0.0.1", 1111);
        final var localTeid = Teid.random();
        final var remoteTeid = Teid.random();
        final var tunnel = (InternalGtpUserTunnel) environment.establishUserPlane("127.0.0.1", 1111);

        final var latch = new CountDownLatch(1);

        // The data we intend to send across the tunnel
        final var message = "awesome";

        final var dataTunnel = tunnel.createDataTunnel(String.class, defaultRemoteAddress, defaultRemotePort)
                .withLocalPort(1212)
                .withLocalIPv4DeviceIp(Buffers.wrapAsIPv4("111.111.111.111"))
                .withRemoteTeid(remoteTeid)
                .withLocalTeid(localTeid)
                .withUserData("some data object")
                .withDecoder(Buffer::toString)
                .withEncoder(Buffers::wrap)
                .onData((t, data) -> {
                    assertThat(data, is(message));
                    latch.countDown();
                })
                .build();

        final var ctx = findConnectionContext(tunnel.id(), defaultBootstrap);
        // remember that we are "receiving" a PDU from the remote endpoint (so really from the PGW)
        // and therefore that incoming PDU will have our local TEID in it.
        final var pdu = somePdu(message, localTeid);
        final var readEvent = GtpMessageReadEvent.of(pdu, connection);
        ctx.match(tunnel, readEvent).apply(tunnel, readEvent);

        // we need a latch here because otherwise we are not actually sure
        // that the onData callback was actually invoked and as such, we wouldn't
        // actually be sure that we did check the incoming data either.
        assertThat(latch.await(10, TimeUnit.MILLISECONDS), is(true));
    }

    /**
     * Note that the way "into" the stack and its processing of events is through the {@link ConnectionContext}.
     * Snice Networking will find and associate every {@link Connection} object with a {@link ConnectionContext},
     * which is then used when invoking the application in the {@link NettyApplicationLayer}
     * <p>
     * Note: for the unit tests, this doesn't matter too much since we just want to have an event processed
     * and as such, grabbing one of the rules.
     */
    final ConnectionContext<GtpTunnel, GtpEvent> findConnectionContext(final ConnectionId id, final GenericBootstrap<GtpTunnel, GtpEvent, TestConfig> bootstrap) {
        return bootstrap.getConnectionContexts()
                .stream()
                .filter(ctx -> ctx.test(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unit Test Error - unable to find an appropriate " +
                        "ConnectionContext. Did you set things up correctly?"));
    }

    /**
     * Whenever we are "trying" to connect to a remote endpoint, we have to mock up a {@link Connection}
     * object that is returned by our GTP-U {@link NetworkInterface}.
     */
    private Connection<GtpEvent> mockGtpUserConnect(final String remoteHost, final int remotePort) {
        final var remoteAddress = new InetSocketAddress(remoteHost, remotePort);
        final var connection = mock(Connection.class);
        when(gtpuNic.connectDirect(Transport.udp, remoteAddress)).thenReturn(connection);
        when(connection.id()).thenReturn(ConnectionId.create(Transport.udp, localAddress, remoteAddress));
        return connection;
    }


    private GtpEnvironment<TestConfig> initializeStack(final DefaultGtpStack<TestConfig> stack,
                                                       final GtpTestApplication gtpApplication,
                                                       final GenericBootstrap<GtpTunnel, GtpEvent, TestConfig> bootstrap,
                                                       final TestConfig config) throws Exception {

        // 1. Snice Networking will call NetworkApp.initialize but our GtpApp will turn that
        //    into a initializeApplication so we call that one here.
        stack.initializeApplication(gtpApplication, bootstrap);

        // 2. Then the Snice Networking will ask the ProtocolBundle to initialize itself and since
        //   our GtpStack is also implementing the ProtocolBundle, we will call it here.
        stack.initialize(config);

        // 3. The Snice Networking will create the underlying NetworkStack and then ask the bundle to create
        //    and environment, so that's what we're doing here
        final var gtpuNicName = config.getConfig().getUserPlane().getNic();
        final var gtpcNicName = config.getConfig().getControlPlane().getNic();
        when(network.getNetworkInterface(gtpuNicName)).thenReturn(Optional.of(gtpuNic));
        when(network.getNetworkInterface(gtpcNicName)).thenReturn(Optional.of(gtpcNic));

        final GtpEnvironment<TestConfig> environment = (GtpEnvironment) stack.createEnvironment(network, config);

        // 4. Then the Snice Networking will start the actual network (which we won't do here) and then
        //    ask the ProtocolBundle to start as well (so again, the GtpStack is also the bundle so...)
        final var future = stack.start(network);
        final var latch = new CountDownLatch(1);
        future.thenAccept(bundle -> latch.countDown());

        if (!latch.await(100, TimeUnit.MILLISECONDS)) {
            Assert.fail("The GtpStack didn't properly start");
        }

        return environment;
    }


}