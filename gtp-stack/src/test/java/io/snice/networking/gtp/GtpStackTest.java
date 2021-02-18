package io.snice.networking.gtp;

import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.app.impl.GenericBootstrap;
import io.snice.networking.bundles.ProtocolBundle;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.core.NetworkInterface;
import io.snice.networking.gtp.conf.GtpAppConfig;
import io.snice.networking.gtp.event.GtpEvent;
import io.snice.networking.gtp.event.GtpMessageWriteEvent;
import io.snice.networking.gtp.impl.DefaultGtpStack;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
@RunWith(MockitoJUnitRunner.class)
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

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        stack = new DefaultGtpStack<TestConfig>();
        environment = initializeStack(stack, "gtp_config_001.yml");
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

    private GtpEnvironment<TestConfig> initializeStack(final DefaultGtpStack<TestConfig> stack, final String configFile) throws Exception {
        final var config = loadConfig(TestConfig.class, "gtp_config_001.yml");

        final GenericBootstrap<GtpTunnel, GtpEvent, TestConfig> bootstrap = new GenericBootstrap<>(config);
        final var gtpApplication = mock(GtpApplication.class);

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

    private static class TestConfig extends GtpAppConfig {

    }

    private static class GtpTestApplication extends GtpApplication<TestConfig> {


        @Override
        public void initialize(final GtpBootstrap<TestConfig> bootstrap) {
            System.err.println("Initializing!");
            bootstrap.onConnection(c -> true).drop();

        }

        @Override
        public void run(final TestConfig configuration, final GtpEnvironment<TestConfig> environment) {
            System.err.println("Running!");
        }
    }

}