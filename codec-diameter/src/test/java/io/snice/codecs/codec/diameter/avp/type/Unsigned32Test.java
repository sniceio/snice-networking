package io.snice.codecs.codec.diameter.avp.type;

import io.snice.buffer.WritableBuffer;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Unsigned32Test {

    @Test
    public void testUnsigned32() {
        final var v = Unsigned32.of(123456L);
        assertThat(v.getValue(), is(123456L));

        final var write = WritableBuffer.of(100);
        v.writeValue(write);

        final var buffer = write.build();
        final var deserialized = Unsigned32.parse(buffer);
        assertThat(deserialized, is(v));
        assertThat(deserialized.getValue(), is(123456L));
    }

}