package io.snice.networking.gtp;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1Message;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1MessageType;
import io.snice.codecs.codec.gtp.gtpc.v1.Gtp1Request;
import io.snice.codecs.codec.gtp.gtpc.v1.impl.ImmutableGtp1Message;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionResponse;
import io.snice.codecs.codec.transport.UdpMessage;
import io.snice.networking.app.ConfigUtils;
import io.snice.networking.gtp.conf.GtpAppConfig;
import org.junit.Before;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static Gtp1Message somePdu() {
        return somePdu(someData, defaultTeid, defaultDeviceIp, defaultDevicePort, defaultRemoteAddress, defaultRemotePort);
    }

    public static Gtp1Message somePdu(final Buffer data,
                                      final Teid remoteTeid,
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
                .withTeid(remoteTeid)
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

}
