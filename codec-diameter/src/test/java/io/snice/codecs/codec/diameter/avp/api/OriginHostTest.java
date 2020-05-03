package io.snice.codecs.codec.diameter.avp.api;

import io.snice.buffer.Buffers;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.impl.DiameterTest;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OriginHostTest extends DiameterTest {

    @Test
    public void testHashCodeAndEquals() throws Exception {
        final var o1 = (OriginHost) loadAndFrameAvp("AVP_Origin_Host.raw").ensure();
        final var o2 = (OriginHost) loadAndFrameAvp("AVP_Origin_Host.raw").ensure();
        ensureHashCodeAndEquals(o1, o2);

        for (final RawDiameterMessageHolder raw : RAW_DIAMETER_MESSAGES) {
            final DiameterMessage msg = DiameterMessage.frame(raw.load());
            raw.assertHeader(msg.getHeader());
            assertThat(msg.getAllAvps().size(), is(raw.avpCount));
        }

        // these two requests both have the same OriginHost
        final var raw0 = RAW_DIAMETER_MESSAGES[0];
        final var raw2 = RAW_DIAMETER_MESSAGES[2];
        final DiameterMessage request1 = DiameterMessage.frame(raw0.load());
        final DiameterMessage request2 = DiameterMessage.frame(raw2.load());
        ensureHashCodeAndEquals(request1.getOriginHost(), request2.getOriginHost());

        final var b1 = Buffers.wrap("mme.epc.mnc001.mcc001.3gppnetwork.org");
        final var b2 = Buffers.wrap("cut this out mme.epc.mnc001.mcc001.3gppnetwork.org strip this").slice(13, 50);
        final var originHos1 = createOriginHost(b1, true);
        final var originHos2 = createOriginHost(b2, false);
        ensureHashCodeAndEquals(originHos1, originHos2);
    }

    private static void ensureHashCodeAndEquals(final OriginHost o1, final OriginHost o2) {
        assertThat(o1.hashCode(), is(o2.hashCode()));
        assertThat(o1, CoreMatchers.is(o2));
    }
}
