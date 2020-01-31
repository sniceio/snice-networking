package io.snice.networking.codec.diameter;

import io.snice.networking.codec.diameter.avp.api.DestinationHost;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DiameterRequestTest extends DiameterTestBase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testCreateCER() {
        DestinationHost.
        final var cer = DiameterRequest.createCER().withDestinationHost().build();
        assertThat(cer.isCER(), is(true));
    }
}
