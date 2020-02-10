package io.snice.networking.codec.diameter;

import io.snice.networking.codec.diameter.avp.api.DestinationHost;
import org.junit.Before;
import org.junit.Test;

public class DiameterRequestTest extends DiameterTestBase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testCreateCER() {
        final DestinationHost dest = DestinationHost.of("hello.epc.mnc001.mcc001.3gppnetwork.org");
        final var cer = DiameterRequest.createCER().withDestinationHost(dest).build();
        // assertThat(cer.isCER(), is(true));
    }
}
