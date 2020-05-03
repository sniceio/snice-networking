package io.snice.codecs.codec.diameter.impl;

import io.snice.buffer.Buffers;
import io.snice.buffer.ReadableBuffer;
import io.snice.buffer.impl.EmptyBuffer;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.DiameterTestBase;
import org.junit.Test;

import static io.snice.codecs.codec.diameter.impl.DiameterParser.couldBeDiameterMessage;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author jonas@jonasborjesson.com
 */
public class DiameterTest extends DiameterTestBase {

    /**
     * Test some basic parsing of diameter messages. We'll just check that we have the right amount of AVPs
     * etc.
     *
     * @throws Exception
     */
    @Test
    public void testParseDiameterMessage() throws Exception {
        for (final RawDiameterMessageHolder raw : RAW_DIAMETER_MESSAGES) {
            final DiameterMessage msg = DiameterMessage.frame(raw.load());
            raw.assertHeader(msg.getHeader());
            assertThat(msg.getAllAvps().size(), is(raw.avpCount));
        }
    }

    @Test
    public void testNoDiameterMessage() throws Exception {
        assertThat(couldBeDiameterMessage(EmptyBuffer.EMPTY.toReadableBuffer()), is(false));
        for (int i = 1; i < 20; ++i) {
            final ReadableBuffer buffer = Buffers.wrap(new byte[i]).toReadableBuffer();
            assertThat(couldBeDiameterMessage(buffer), is(false));
        }
    }
}
