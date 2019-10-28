package io.snice.networking.codec.diameter.impl;

import io.snice.networking.codec.diameter.DiameterRequest;
import io.snice.networking.codec.diameter.DiameterTestBase;
import io.snice.networking.codec.diameter.avp.api.VendorId;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CapabilitiesExchangeRest extends DiameterTestBase {

    private DiameterRequest cer;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        cer = loadDiameterMessage("capabilities_exchange_request.raw").toRequest();
    }

    @Test
    public void frameCER() throws Exception {
        final var origHost = cer.getOriginHost();
        assertThat(origHost.getValue().asString(), is("seagull.node.epc.mnc001.mcc001.3gppnetwork.org"));

        final var origRealm = cer.getOriginRealm();
        assertThat(origRealm.getValue().asString(), is("epc.mnc001.mcc001.3gppnetwork.org"));

        final var productName = cer.getAvp(VendorId.CODE);
        final var vendorId = (VendorId) productName.get().parse();
        assertThat(vendorId.getValue().getValue(), is(10415L));
    }

    @Test
    public void testCreateCEA() throws Exception {
        final var cea = cer.createAnswer(null)
                .build();
    }
}
