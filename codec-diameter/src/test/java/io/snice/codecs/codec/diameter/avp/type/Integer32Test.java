package io.snice.codecs.codec.diameter.avp.type;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Integer32Test {


    @Test
    public void testInteger32() {
        assertThat(Integer32.of(222).getValue(), is(222));
    }

    @Test
    public void testHasCodeEquals() {
        final var one = Integer32.of(2134);
        final var two = Integer32.of(2134);

        assertThat(one.hashCode(), is(two.hashCode()));
        assertThat(one, is(two));
    }

}
