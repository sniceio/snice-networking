package io.snice.networking.codec.diameter.impl;

import io.snice.networking.codec.diameter.avp.Vendor;
import io.snice.networking.codec.diameter.avp.api.ExperimentalResultCode;
import io.snice.networking.codec.diameter.avp.api.VendorId;
import org.junit.Test;

import static io.snice.networking.codec.diameter.avp.api.ExperimentalResultCode.DiameterErrorInvalidApplicationCode5632;
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
        assertThat(vendorId.getValue().getValue(), is(Vendor.TGPP.getCode()));
    }

}