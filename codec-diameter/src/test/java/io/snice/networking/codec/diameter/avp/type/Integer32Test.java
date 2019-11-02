package io.snice.networking.codec.diameter.avp.type;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class Integer32Test {


    @Test
    public void testInteger32() {
        Assert.assertThat(Integer32.of(222).getValue(), CoreMatchers.is(222));
    }

}
