package io.snice.networking.gtp;

import io.snice.networking.gtp.impl.DefaultGtpUserTunnel;
import io.snice.networking.gtp.impl.InternalGtpStack;
import io.snice.networking.gtp.impl.InternalGtpUserTunnel;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;

public class GtpUserTunnelTest extends GtpStackTestBase {

    @Mock
    private InternalGtpStack stack;

    /**
     * Very simple test that just ensures that the {@link InternalGtpUserTunnel} actually delegates
     * the {@link DataTunnel} creation to the stack, since it is in charge of everything.
     */
    @Test
    public void testEstablishDataTunnel() {
        final var tunnel = (InternalGtpUserTunnel) DefaultGtpUserTunnel.of(defaultConnectionId, stack);
        tunnel.createDataTunnel(String.class, defaultRemoteAddress, defaultRemotePort);

        verify(stack).createDataTunnel(tunnel, String.class, defaultRemoteAddress, defaultRemotePort);
    }
}