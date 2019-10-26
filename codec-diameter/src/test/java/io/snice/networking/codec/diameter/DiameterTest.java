package io.snice.networking.codec.diameter;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.buffer.impl.EmptyBuffer;
import org.junit.Test;

import static io.snice.networking.codec.diameter.impl.DiameterParser.couldBeDiameterMessage;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author jonas@jonasborjesson.com
 */
public class DiameterTest {

    @Test
    public void testReadDiameterPcap() throws Exception {

    }

    @Test
    public void testNoDiameterMessage() throws Exception {
        assertThat(couldBeDiameterMessage(EmptyBuffer.EMPTY), is(false));
        for (int i = 1; i < 20; ++i) {
            final Buffer buffer = Buffers.wrap(new byte[i]);
            assertThat(couldBeDiameterMessage(buffer), is(false));
        }
    }
}
