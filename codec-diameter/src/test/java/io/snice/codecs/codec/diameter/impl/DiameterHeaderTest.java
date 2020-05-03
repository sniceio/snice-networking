package io.snice.codecs.codec.diameter.impl;

import io.snice.codecs.codec.diameter.DiameterHeader;
import io.snice.codecs.codec.diameter.DiameterTestBase;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author jonas@jonasborjesson.com
 */
public class DiameterHeaderTest extends DiameterTestBase {

    /**
     * Ensure that we can ensure and read the diameter headers. We'll check all the values that are part
     * of header.
     *
     * @throws Exception
     */
    @Test
    public void testParseDiameterHeader() throws Exception {
        for (final RawDiameterMessageHolder raw : RAW_DIAMETER_MESSAGES) {
            final DiameterHeader header = raw.getHeader();
            raw.assertHeader(header);
            assertTrue("Expected the header for resource " + raw.resource + " to be validated to true", header.validate());
        }
    }

    @Test
    public void testCloneDiameterHeader() {
        final var air = RAW_DIAMETER_MESSAGES[0].getHeader();
        final var aia = air.copy().isAnswer().build();

        assertThat(aia.isRequest(), is(false));
        assertThat(aia.isAnswer(), is(true));
        assertThat(air.getApplicationId(), is(aia.getApplicationId()));
        assertThat(air.getEndToEndId(), is(aia.getEndToEndId()));
        assertThat(air.getHopByHopId(), is(aia.getHopByHopId()));
    }

    @Test
    public void testCreateDiameterHeader() {
        final var header = DiameterHeader.of().withCommandCode(123).isAnswer().withEndToEndId(5555L).build();
        assertThat(header.getVersion(), is(1));
        assertThat(header.isAnswer(), is(true));
        assertThat(header.getCommandCode(), is(123));
        assertThat(header.getEndToEndId(), is(5555L));
    }

}
