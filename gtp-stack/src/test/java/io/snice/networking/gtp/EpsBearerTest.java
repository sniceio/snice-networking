package io.snice.networking.gtp;

import io.snice.buffer.Buffers;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.networking.gtp.impl.InternalGtpUserTunnel;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EpsBearerTest extends GtpStackTestBase {

    private EpsBearer bearer;
    private InternalGtpUserTunnel tunnel;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        tunnel = mock(InternalGtpUserTunnel.class);
        bearer = EpsBearer.create(tunnel, defaultPdnSessionContext, 12345);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBearerWrongTunnelType() {
        EpsBearer.create(mock(GtpUserTunnel.class), defaultPdnSessionContext, 12345);
    }

    /**
     * When creating a {@link DataTunnel} via the {@link EpsBearer}, the bearer will
     * "fill out" certain information since the {@link DataTunnel} is created within the context
     * of the given bearer. This test just makes sure that we do extract out the correct
     * pieces from the {@link PdnSessionContext} within the bearer.
     */
    @Test
    public void testCreateUserDataTunnelExtractBearerInfo() {
        final var builder = mockDataTunnelBuilder();
        when(tunnel.createDataTunnel(String.class, "10.11.12.13", 3333)).thenReturn(builder);

        final int localPort = 12345;
        final var bearer = EpsBearer.create(tunnel, defaultPdnSessionContext, localPort);
        bearer.createDataTunnel(String.class, "10.11.12.13", 3333);

        // The bearer should "fill in" the local and remote teid, the IP address allocated
        // to the device (which is part of the PAA of the CreateSessionResponse).
        // Note: the values were verified using Wireshark and you can see 
        // them all in the PdnSessionTest where it checks for these...
        verify(builder).withLocalTeid(Teid.of(0x12, 0xed, 0xe3, 0x72));
        verify(builder).withRemoteTeid(Teid.of(0x4c, 0xa4, 0x0c, 0xe5));
        verify(builder).withLocalIPv4DeviceIp(Buffers.wrapAsIPv4("100.64.12.229"));
        verify(builder).withLocalPort(localPort);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateUserDataTunnelBadInput001() {
        bearer.createDataTunnel(null, "10.11.12.13", 3333).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateUserDataTunnelBadInput002() {
        bearer.createDataTunnel(String.class, null, 3333).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateUserDataTunnelBadInput003() {
        bearer.createDataTunnel(String.class, "", 3333).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateUserDataTunnelBadInput004() {
        bearer.createDataTunnel(String.class, "11.11.11.11", -1).build();
    }

}