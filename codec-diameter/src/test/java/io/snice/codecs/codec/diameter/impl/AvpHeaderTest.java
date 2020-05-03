package io.snice.codecs.codec.diameter.impl;

import io.snice.codecs.codec.diameter.DiameterTestBase;
import io.snice.codecs.codec.diameter.avp.Avp;
import io.snice.codecs.codec.diameter.avp.AvpHeader;
import io.snice.codecs.codec.diameter.avp.FramedAvp;
import io.snice.codecs.codec.diameter.avp.api.OriginHost;
import io.snice.codecs.codec.diameter.avp.api.OriginRealm;
import io.snice.codecs.codec.diameter.avp.api.ResultCode;
import io.snice.codecs.codec.diameter.avp.type.DiameterIdentity;
import io.snice.codecs.codec.diameter.avp.type.Enumerated;
import io.snice.codecs.codec.diameter.avp.type.Grouped;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

// import io.snice.networking.codec.diameter.diameter.avp.VendorSpecificApplicationId2;

/**
 * Tests for verifying the {@link AvpHeader}.
 *
 * @author jonas@jonasborjesson.com
 */
public class AvpHeaderTest extends DiameterTestBase {

    @Test
    public void testAvpHeader() throws Exception {
        for (final RawAvpHolder raw : RAW_AVPS) {
            final AvpHeader header = raw.getHeader();
            raw.assertHeader(header);
        }
    }

    @Test
    public void testBasicAvp() throws Exception {
        for (final RawAvpHolder raw : RAW_AVPS) {
            final FramedAvp avp = raw.getAvp();
            raw.assertHeader(avp.getHeader());
        }
    }

    @Test
    public void testBuildAvpHeader() {
        final var header = AvpHeader.withCode(100).isMandatory().build();
        assertThat(header.getCode(), is(100L));
        assertThat(header.isMandatory(), is(true));
        assertThat(header.isProtected(), is(false));
        assertThat(header.isVendorSpecific(), is(false));
        assertThat(header.getVendorId().isEmpty(), is(true));

        // get the raw buffer out and then reparse it and see
        // that all is well
        final var buffer = header.getBuffer();
        final var h2 = AvpHeader.frame(buffer.toReadableBuffer());

        assertThat(h2.getCode(), is(100L));
        assertThat(h2.isMandatory(), is(true));
        assertThat(h2.isProtected(), is(false));
        assertThat(h2.isVendorSpecific(), is(false));
        assertThat(h2.getVendorId().isEmpty(), is(true));
    }

    @Test
    public void testOriginHost() throws Exception {
        final FramedAvp raw = loadAndFrameAvp("AVP_Origin_Host.raw");
        final Avp avp = raw.ensure();
        assertThat(avp instanceof OriginHost, is(true));
        assertThat(avp.getCode(), is(264L));
        assertThat((OriginHost) avp instanceof OriginHost, is(true));

        final DiameterIdentity identity = (DiameterIdentity) avp.getValue();
        assertThat(identity.asString(), is("mme.epc.mnc001.mcc001.3gppnetwork.org"));
    }

    @Test
    public void testOriginRealm() throws Exception {
        final FramedAvp raw = loadAndFrameAvp("AVP_Origin_Realm.raw");
        final Avp avp = raw.ensure();
        assertThat(avp instanceof OriginRealm, is(true));
        assertThat((OriginRealm) avp instanceof OriginRealm, is(true));

        final OriginRealm originRealm = (OriginRealm) avp;
        final DiameterIdentity identity = originRealm.getValue();
        assertThat(identity.asString(), is("epc.mnc001.mcc001.3gppnetwork.org"));
    }

    @Test
    public void testResultCode() throws Exception {
        final FramedAvp raw = loadAndFrameAvp("AVP_Result_Code.raw");
        final Avp<Enumerated<ResultCode.Code>> avp = raw.ensure();
        // final Avp avp = raw.ensure();
        assertThat(avp.isEnumerated(), is(true));
        assertThat(avp.getCode(), is(268L));
        final Avp<Enumerated<ResultCode.Code>> result = avp.toEnumerated();
        final ResultCode.Code rse = result.getValue().getAsEnum().get();
        assertThat(rse, is(ResultCode.Code.DIAMETER_SUCCESS_2001));
    }

    @Test
    public void testGroupedAvp() throws Exception {
        final FramedAvp raw = loadAndFrameAvp("AVP_Vendor_Specific_Application.raw");
        final Avp<Grouped> avp = raw.ensure();
        assertThat(avp.getCode(), is(260L));
        final Grouped grouped = avp.getValue();

        // Test to get the "raw" un-parsed AVPs...
        // It is important that doing it the "raw" way doesn't then
        // affect to ensure it later on, which is what would happen if
        // you don't watch out for how the internal buffers are handled.
        // I.e., if you share the same reader-index then reading from the
        // buffer below will then effect subsequent reading from the AVP.
        final FramedAvp vendorId = grouped.getFramedAvp(266).get();
        final FramedAvp authAppId = grouped.getFramedAvp(258).get();
        assertThat(vendorId.getData().getUnsignedInt(0), is(10415L));
        assertThat(authAppId.getData().getUnsignedInt(0), is(16777251L));

        // now test to use the convenience methods that will ensure it all out
        // and also return the correct types
        // final VendorSpecificApplicationId2 vsaid = (VendorSpecificApplicationId2) avp;
        // assertThat(vsaid.getVendorId().getValue().getValue(), is(10415L));
        // assertThat(vsaid.getAuthApplicationId().get().getValue().getValue(), is(16777251L));

        assertThat(grouped, notNullValue());
    }

}
