package io.snice.networking.codec.gtp.gtpc;

import io.snice.networking.codec.gtp.GtpRawData;
import io.snice.networking.codec.gtp.gtpc.v2.tliv.IMSI;
import io.snice.networking.codec.gtp.gtpc.v2.tliv.TypeLengthInstanceValue;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IMSITest {

    @Test
    public void testFrameIMSI() {
        // just the raw IMSI
        var imsi = IMSI.frame(GtpRawData.imsi);
        assertThat(imsi.toString(), is("99999123456789"));

        // the full TLIV version of the IMSI
        imsi = TypeLengthInstanceValue.frame(GtpRawData.imsiTLIV).ensure().toIMSI();
        assertThat(imsi.toString(), is("99999123456789"));
    }
}
