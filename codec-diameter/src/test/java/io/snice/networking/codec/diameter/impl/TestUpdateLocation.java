package io.snice.networking.codec.diameter.impl;

import io.snice.networking.codec.diameter.DiameterAnswer;
import io.snice.networking.codec.diameter.DiameterRequest;
import io.snice.networking.codec.diameter.DiameterTestBase;
import io.snice.networking.codec.diameter.avp.api.Msisdn;
import io.snice.networking.codec.diameter.avp.api.ResultCode;
import io.snice.networking.codec.diameter.avp.api.SubscriberStatus;
import io.snice.networking.codec.diameter.avp.api.SubscriptionData;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestUpdateLocation extends DiameterTestBase {
    private DiameterRequest ulr;
    private DiameterAnswer ula;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ulr = loadDiameterMessage("ulr.raw").toRequest();
        ula = loadDiameterMessage("ula.raw").toAnswer();
    }

    @Test
    public void testFrameULR() throws Exception {
        final var origHost = ulr.getOriginHost();
        assertThat(origHost.getValue().asString(), is("seagull.node.epc.mnc001.mcc001.3gppnetwork.org"));

        final var origRealm = ulr.getOriginRealm();
        assertThat(origRealm.getValue().asString(), is("epc.mnc001.mcc001.3gppnetwork.org"));
    }

    @Test
    public void testFrameULA() throws Exception {
        final var origHost = ula.getOriginHost();
        assertThat(origHost.getValue().asString(), is("127.0.0.1"));

        final var origRealm = ula.getOriginRealm();
        assertThat(origRealm.getValue().asString(), is("epc.mnc001.mcc001.3gppnetwork.org"));

        // TODO: will redo the result codes at some point to only have fixed classes
        // that are essentially used as enums
        final var resultCode = ula.getResultCode();
        assertThat(resultCode.getRight().getAsEnum().get(), is(ResultCode.DiameterSuccess2001.getAsEnum().get()));

        final var subscriptionData = (SubscriptionData) ula.getAvp(SubscriptionData.CODE).get().ensure();

        final var subscriberStatus = (SubscriberStatus) subscriptionData.getValue().getFramedAvp(SubscriberStatus.CODE).get().ensure();
        assertThat(subscriberStatus.getAsEnum().get(), is(SubscriberStatus.Code.SERVICE_GRANTED));

        final var msisdn = (Msisdn) subscriptionData.getValue().getFramedAvp(Msisdn.CODE).get().ensure();
        assertThat(msisdn.getValue().getValue(), is("43939393939393930303"));
        assertThat(msisdn.getPadding(), is(2));
    }

}
