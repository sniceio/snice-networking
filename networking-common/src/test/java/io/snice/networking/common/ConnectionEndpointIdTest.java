package io.snice.networking.common;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class ConnectionEndpointIdTest {

    @Test
    public void testCreateEndpointIdWithBuffer() {
        final var buffer = Buffers.wrapAsIPv4("10.11.12.13");
        final var id = ConnectionEndpointId.create(Transport.udp, buffer, 1111);
        assertThat(id.getIpAddress(), is("10.11.12.13"));
        assertThat(id.getProtocol(), is(Transport.udp));
        assertThat(id.getPort(), is(1111));

        assertThat(id.toString(), is("udp:10.11.12.13:1111"));
    }

    @Test
    public void testBadInputs() {
        ensureBadInput(null);
        ensureBadInput(Buffer.of((byte) 0x00));
        ensureBadInput(Buffer.of((byte) 0x00, (byte) 0x01));
        ensureBadInput(Buffer.of((byte) 0x00, (byte) 0x01, (byte) 0x02));

        // jumping to 5 bytes since 4 is what we want
        ensureBadInput(Buffer.of((byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04));
    }

    private void ensureBadInput(final Buffer buffer) {
        try {
            ConnectionEndpointId.create(Transport.udp, buffer, 1111);
            fail("Expected to blow up on a " + IllegalArgumentException.class.getName() + " due to bad input");
        } catch (final IllegalArgumentException e) {
            // expected
        }
    }

}