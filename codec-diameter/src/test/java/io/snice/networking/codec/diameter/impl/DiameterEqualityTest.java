package io.snice.networking.codec.diameter.impl;

import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.DiameterRequest;
import io.snice.networking.codec.diameter.DiameterTestBase;
import io.snice.networking.codec.diameter.avp.api.ResultCode;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class DiameterEqualityTest extends DiameterTestBase {


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testBasicEquality() {
        final var a = DiameterRequest.createRequest(123)
                .withOriginHost("epc.mnc001.mcc001.3gppnetwork.org")
                .withOriginRealm("epc.mnc001.mcc001.3gppnetwork.org")
                .withDestinationRealm("epc.mnc999.mcc999.3gppnetwork.org")
                .build();

        final var b = DiameterRequest.createRequest(123)
                .withOriginHost("epc.mnc001.mcc001.3gppnetwork.org")
                .withOriginRealm("epc.mnc001.mcc001.3gppnetwork.org")
                .withDestinationRealm("epc.mnc999.mcc999.3gppnetwork.org")
                .build();

        ensureEquality(a, b);

        final var aA = a.createAnswer(ResultCode.DiameterSuccess2001).build();
        final var bA = b.createAnswer(ResultCode.DiameterSuccess2001).build();
        ensureEquality(aA, bA);

        ensureNotEquals(aA, a);
        ensureNotEquals(aA, b);
        ensureNotEquals(bA, b);
        ensureNotEquals(bA, a);
    }

    private static void ensureEquality(final DiameterMessage a, final DiameterMessage b) {
        assertThat(a, is(b));
        assertThat(b, is(a));
        assertThat(a, is(a));
        assertThat(b, is(b));
    }

    private static void ensureNotEquals(final DiameterMessage a, final DiameterMessage b) {
        assertThat(a, not(b));
        assertThat(b, not(a));
    }


}