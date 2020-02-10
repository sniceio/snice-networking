package io.snice.networking.codec.gtp.gtpc;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.gtp.GtpRawData;
import io.snice.networking.codec.gtp.GtpTestBase;
import io.snice.networking.codec.gtp.gtpc.v2.tliv.TypeLengthInstanceValue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TypeLengthInstanceValueTest extends GtpTestBase {

    @Before
    public void setUp() throws Exception {
    }

    @Ignore
    @Test
    public void testParseImsiTLIV() {
        ensureBasicTlivProperties(GtpRawData.imsiTLIV, 1, 8);
        ensureBasicTlivProperties(GtpRawData.userLocationInfo, 86, 13);
        ensureBasicTlivProperties(GtpRawData.servingNetwork, 83, 3);
        ensureBasicTlivProperties(GtpRawData.fteid, 87, 9);
    }

    @Test
    public void testParseIMSI() throws Exception {
        final TypeLengthInstanceValue tliv = TypeLengthInstanceValue.frame(GtpRawData.imsiTLIV);
        assertThat(tliv.isIMSI(), is(true));
        final var imsi = tliv.ensure().toIMSI();

    }

    private static void ensureBasicTlivProperties(final Buffer buffer, final int expectedType, final int expectedLength) {
        final TypeLengthInstanceValue tliv = TypeLengthInstanceValue.frame(buffer);
        assertThat(tliv.getTypeAsDecimal(), is(expectedType));
        assertThat(tliv.getLength(), is(expectedLength));
    }
}