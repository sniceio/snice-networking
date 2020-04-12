package io.snice.networking.codec.diameter;

import io.snice.networking.codec.diameter.avp.api.DestinationHost;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DiameterRequestTest extends DiameterTestBase {

    private final DestinationHost dest = DestinationHost.of("hello.epc.mnc001.mcc001.3gppnetwork.org");
    private final String imsi = "9999991234";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testCreateCER() {
        final var cer = DiameterRequest.createCER().withDestinationHost(dest).build();
        assertThat(cer.isCER(), is(true));
    }

    @Test
    public void testCreateULR() {
        final var b = DiameterRequest.createULR();
        b.withDestinationHost("hello.world.epc.mnc001.mcc001.3gppnetwork.org");
        b.withDestinationRealm("epc.mnc999.mcc999.3gppnetwork.org");
        b.withUserName(imsi);

        final var ulr = b.build();
        assertThat(ulr.isULR(), is(true));
        final var header = ulr.getHeader();
        assertThat(header.getApplicationId(), is(16777251L));
        assertThat(header.getCommandCode(), is(316));
    }
}
