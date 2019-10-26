package io.snice.networking.codec.diameter.impl;

import io.snice.networking.codec.diameter.AvpHeader;
import io.snice.networking.codec.diameter.DiameterTestBase;
import org.junit.Test;

/**
 * Tests for verifying the {@link AvpHeader}.
 *
 * @author jonas@jonasborjesson.com
 */
public class AvpHeaderTest extends DiameterTestBase {

    @Test
    public void testAvpHeader() throws Exception {

        ensureAvpHeader("001_diameter_auth_info_request.raw", 344);
    }

    private static void ensureAvpHeader(final String resource, final int code) throws Exception {
        final AvpHeader header = AvpHeader.frame(loadBuffer(resource));
    }
}
