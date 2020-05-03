package io.snice.codecs.codec.diameter.impl;

import io.snice.codecs.codec.diameter.avp.Vendor;
import io.snice.codecs.codec.diameter.avp.api.ExperimentalResultCode;
import io.snice.codecs.codec.diameter.avp.api.VendorId;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static io.snice.codecs.codec.diameter.avp.api.ExperimentalResultCode.DiameterErrorInvalidApplicationCode5632;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ExperimentalResultHelperTest {

    @Test
    public void testCreateExperimentalResult() {
        final var result = ExperimentalResultHelper.map(DiameterErrorInvalidApplicationCode5632);
        assertThat(result.isExperimentalResult(), is(true));

        final var resultCode = result.getValue().getFramedAvp(ExperimentalResultCode.CODE).get().ensure().toExperimentalResultCode();
        assertThat(resultCode.getCode(), is(DiameterErrorInvalidApplicationCode5632.getCode()));

        final var vendorId = (VendorId)result.getValue().getFramedAvp(VendorId.CODE).get().ensure();
        assertThat(vendorId.getValue().getValue(), CoreMatchers.is(Vendor.TGPP.getCode()));
    }

}