package io.snice.networking.codec.diameter.impl;

import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.DiameterRequest;
import io.snice.networking.codec.diameter.DiameterTestBase;
import io.snice.networking.codec.diameter.avp.api.ResultCode;
import io.snice.networking.codec.diameter.avp.api.VendorId;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CapabilitiesExchangeTest extends DiameterTestBase {

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
        final var vendorId = (VendorId) productName.get().ensure();
        assertThat(vendorId.getValue().getValue(), is(10415L));
    }

    @Test
    public void testCreateCEA() {
        final var cea = cer.createAnswer(ResultCode.DiameterSuccess).build();
        assertThat(cea.isCEA(), is(true));
        assertThat(cea.getResultCode(), is(ResultCode.DiameterSuccess));
        assertThat(cea.getOriginHost().getValue().asString(), is("seagull.node.epc.mnc001.mcc001.3gppnetwork.org"));
        assertThat(cea.getOriginRealm().getValue().asString(), is("epc.mnc001.mcc001.3gppnetwork.org"));

        // make sure that we actually created the underlying byte-array correctly
        // and the easiest way is just to re-frame it.
        final var buffer = cea.getBuffer();
        final var ceaClone = DiameterMessage.frame(buffer).toAnswer();
        assertThat(ceaClone.isCEA(), is(true));
        assertThat(ceaClone.isAnswer(), is(true));
        assertThat(ceaClone.getOriginHost().getValue().asString(), is("seagull.node.epc.mnc001.mcc001.3gppnetwork.org"));
        assertThat(ceaClone.getOriginRealm().getValue().asString(), is("epc.mnc001.mcc001.3gppnetwork.org"));
    }
}
