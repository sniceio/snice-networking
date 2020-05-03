package io.snice.codecs.codec.diameter.avp.type;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.diameter.avp.api.Msisdn;
import io.snice.codecs.codec.diameter.impl.DiameterTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OctetStringTest extends DiameterTest {

    @Test
    public void parseMSISDN() throws Exception {
        final var msisdn = (Msisdn) loadAndFrameAvp("AVP_MSISDN.raw").ensure();
        assertThat(msisdn.getPadding(), is(2));


        // TODO: need to figure out a good way for OctetString to also take into
        // consideration the underlying encoding... Or, do we do this when parsing?
        // probably not because we want to keep the formatting as we got it over the
        // wire.
        assertThat(msisdn.getValue().getValue(), is("43939393939393930303"));
    }

    @Test
    public void testHashCodeEquals() throws Exception {
        final var one = (Msisdn) loadAndFrameAvp("AVP_MSISDN.raw").ensure();
        final var two = (Msisdn) loadAndFrameAvp("AVP_MSISDN.raw").ensure();
        assertThat(one.hashCode(), is(two.hashCode()));
        assertThat(one, is(two));

        ensureHashCodeAndEquals(Buffers.wrap("hello"), Buffers.wrap("hello"));

        final var b1 = Buffers.wrap("hello world ena goa grejor").slice(6, 11);
        final var b2 = Buffers.wrap("world");
        ensureHashCodeAndEquals(b1, b2);
    }

    private static void ensureHashCodeAndEquals(final Buffer one, final Buffer two) {
        final var octect1 = OctetString.parse(one);
        final var octect2 = OctetString.parse(two);
        assertThat(octect1.hashCode(), is(octect2.hashCode()));
        assertThat(octect1, is(octect2));

    }
}
