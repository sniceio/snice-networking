package io.snice.codecs.codec.diameter.avp.api;

import io.snice.codecs.codec.diameter.DiameterTestBase;
import io.snice.codecs.codec.diameter.avp.Avp;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class VendorSpecificApplicationIdTest extends DiameterTestBase {

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromNullArray() {
        VendorSpecificApplicationId.of((Avp[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFromEmptyArray() {
        VendorSpecificApplicationId.of(new Avp[0]);
    }

    @Test
    public void testCreate() {
        final var vendorId = VendorId.of(10415L);
        final var authId = AuthApplicationId.of(16777251L);
        final var acctId = AcctApplicationId.of(0L);

        final var app = VendorSpecificApplicationId.of(vendorId, authId, acctId);

        assertThat(app, notNullValue());

        final var avps = app.getAvps();
        assertThat(avps.size(), is(3));
        assertThat(avps.get(0), is(vendorId));
        assertThat(avps.get(1), is(authId));
        assertThat(avps.get(2), is(acctId));

        // find by code
        assertThat(app.getFramedAvp(AuthApplicationId.CODE).get(), is(authId));
        assertThat(app.getFramedAvp(AcctApplicationId.CODE).get(), is(acctId));
        assertThat(app.getFramedAvp(VendorId.CODE).get(), is(vendorId));
    }

}