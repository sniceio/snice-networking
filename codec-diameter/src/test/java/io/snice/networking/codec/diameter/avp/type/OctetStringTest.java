package io.snice.networking.codec.diameter.avp.type;

import io.snice.networking.codec.diameter.avp.api.Msisdn;
import io.snice.networking.codec.diameter.impl.DiameterTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OctetStringTest extends DiameterTest {

    @Test
    public void parseMSISDN() throws Exception {
        final var msisdn = (Msisdn) DiameterTest.loadAndFrameAvp("AVP_MSISDN.raw").ensure();
        assertThat(msisdn.getPadding(), is(2));


        // TODO: need to figure out a good way for OctetString to also take into
        // consideration the underlying encoding... Or, do we do this when parsing?
        // probably not because we want to keep the formatting as we got it over the
        // wire.
        assertThat(msisdn.getValue().getValue(), is("43939393939393930303"));
    }
}
