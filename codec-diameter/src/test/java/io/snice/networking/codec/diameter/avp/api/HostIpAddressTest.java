package io.snice.networking.codec.diameter.avp.api;

import io.snice.networking.codec.diameter.DiameterTestBase;
import io.snice.networking.codec.diameter.avp.type.IpAddress;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HostIpAddressTest extends DiameterTestBase {

    /**
     * @throws Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testCreateHostIpAddress() {
        final var hostIP = HostIpAddress.of(IpAddress.createIpv4Address("10.36.10.10"));
        assertThat(hostIP.isHostIpAddress(), is(true));
        assertThat(hostIP.getValue().isIPv4(), is(true));
        assertThat(hostIP.getValue().isIPv6(), is(false));
        assertThat(hostIP.getValue().asString(), is("10.36.10.10"));
    }

}