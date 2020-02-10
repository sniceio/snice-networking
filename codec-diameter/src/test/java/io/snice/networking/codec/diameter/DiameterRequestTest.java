package io.snice.networking.codec.diameter;

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
        // final DestinationHost dest = DestinationHost.of("hello.epc.mnc001.mcc001.3gppnetwork.org");
        // final var cer = DiameterRequest.createCER().withDestinationHost().build();
        // assertThat(cer.isCER(), is(true));
    }
}
