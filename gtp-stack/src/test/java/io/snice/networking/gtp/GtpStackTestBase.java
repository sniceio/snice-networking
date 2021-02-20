package io.snice.networking.gtp;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1Message;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1MessageType;
import io.snice.codecs.codec.gtp.gtpc.v1.impl.ImmutableGtp1Message;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionResponse;
import io.snice.codecs.codec.transport.UdpMessage;
import io.snice.networking.app.ConfigUtils;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.common.Transport;
import io.snice.networking.gtp.conf.GtpAppConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GtpStackTestBase {

    protected PdnSessionContext defaultPdnSessionContext;
    protected CreateSessionRequest req;
    protected CreateSessionResponse res;

    /**
     * Random IP assigned to the device.
     */
    protected static final String defaultDeviceIp = "10.11.12.13";

    /**
     * Random port used in the IP packet within the PDU when the device is "sending" or "receiving"
     * packets.
     */
    protected static final int defaultDevicePort = 3333;

    /**
     * Random IP to a random remote host that we are sending traffic to.
     */
    protected static final String defaultRemoteAddress = "20.30.40.50";

    /**
     * Random port to a random remote port that we are sending/receiving traffic to/from.
     */
    protected static final int defaultRemotePort = 3333;


    /**
     * Just a random TEID used with various types of testing.
     */
    protected static final Teid defaultTeid = Teid.random();

    /**
     * Some tests require us to pass in, or use, a local address. We'll just
     * make one up since it in general doesn't matter. There are some tests, however,
     * that require the local address to match that of the configured listening
     * addresses (what you've specified in the config files) and in those cases
     * we have to actually use it.
     */
    protected static final InetSocketAddress localAddress = new InetSocketAddress("127.0.0.1", 7777);

    protected static final InetSocketAddress remoteAddress = new InetSocketAddress(defaultRemoteAddress, defaultRemotePort);

    /**
     * Just some default {@link ConnectionId} for those tests that just need one but otherwise don't care too much
     * about it.
     */
    protected static final ConnectionId defaultConnectionId = ConnectionId.create(Transport.udp, localAddress, remoteAddress);

    protected static Buffer someData = Buffers.wrap("hello world");

    @Before
    public void setUp() throws Exception {
        req = GtpMessage.frame(loadRaw("create_session_request.raw")).toGtp2Request().toCreateSessionRequest();
        res = (CreateSessionResponse) GtpMessage.frame(loadRaw("create_session_response.raw")).toGtp2Response();

        // Note: See the {@link PdnSessionTest#testCreatePdnSessionContext} for the values
        // within the given request/response in case you end up using this context and need to
        // e.g. check the {@link Teid} or something.
        defaultPdnSessionContext = PdnSessionContext.of(req, res);
    }

    public static Buffer loadRaw(final String resource) throws Exception {
        final Path path = Paths.get(GtpStackTestBase.class.getResource(resource).toURI());
        final byte[] content = Files.readAllBytes(path);
        return Buffer.of(content);
    }

    public static Gtp1Message somePdu(final String data, final Teid teid) {
        return somePdu(Buffers.wrap(data), teid);
    }

    public static Gtp1Message somePdu(final Buffer data, final Teid teid) {
        return somePdu(data, teid, defaultDeviceIp, defaultDevicePort, defaultRemoteAddress, defaultRemotePort);
    }

    public static Gtp1Message somePdu() {
        return somePdu(someData, defaultTeid, defaultDeviceIp, defaultDevicePort, defaultRemoteAddress, defaultRemotePort);
    }

    /**
     * Setup the mock for the {@link DataTunnel.Builder}, which is just mocking up
     * to return the builder for every withXXX method so the code being tested
     * doesn't blow up since it will typically use the fluent API.
     *
     * @return
     */
    public static DataTunnel.Builder mockDataTunnelBuilder() {
        final var builder = mock(DataTunnel.Builder.class);
        when(builder.withLocalTeid(any())).thenReturn(builder);
        when(builder.withRemoteTeid(any())).thenReturn(builder);
        when(builder.withLocalIPv4DeviceIp(any())).thenReturn(builder);
        when(builder.withLocalPort(anyInt())).thenReturn(builder);
        return builder;
    }

    public static Gtp1Message somePdu(final Buffer data,
                                      final Teid teid,
                                      final String deviceIp, final int devicePort,
                                      final String remoteAddress, final int remotePort) {
        final var ipv4 = UdpMessage.createUdpIPv4(data)
                .withDestinationPort(remotePort)
                .withSourcePort(devicePort)
                .withTTL(64)
                .withDestinationIp(remoteAddress)
                .withSourceIp(deviceIp)
                .build();

        return ImmutableGtp1Message.create(Gtp1MessageType.G_PDU)
                .withTeid(teid)
                .withPayload(ipv4.getBuffer())
                .build();
    }

    protected GtpAppConfig loadConfig(final String config) throws Exception {
        final var buffer = loadRaw(config);
        return ConfigUtils.loadConfiguration(GtpAppConfig.class, buffer.getContent());
    }

    protected <T extends GtpAppConfig> T loadConfig(final Class<T> clz, final String config) throws Exception {
        final var buffer = loadRaw(config);
        return ConfigUtils.loadConfiguration(clz, buffer.getContent());
    }

    /**
     * Only so that when you run unit test within IntelliJ and selects "run all tests" from the top-level
     * module, it doesn't complain that this test base doesn't have any tests in it (it's because of the
     * RunWith annotation and seems it decides to try and run this one too. From Maven, this is not an issue)
     */
    @Test
    public void dumb() {
    }

    protected static class TestConfig extends GtpAppConfig {

    }

    protected static class GtpTestApplication extends GtpApplication<TestConfig> {

        private final Consumer<GtpBootstrap<TestConfig>> rulesFunction;

        protected GtpTestApplication(final Consumer<GtpBootstrap<TestConfig>> rulesFunction) {
            this.rulesFunction = rulesFunction;
        }


        @Override
        public void initialize(final GtpBootstrap<TestConfig> bootstrap) {
            rulesFunction.accept(bootstrap);
        }

        @Override
        public void run(final TestConfig configuration, final GtpEnvironment<TestConfig> environment) {
            System.err.println("Running!");
        }
    }


}
