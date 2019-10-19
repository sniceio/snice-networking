package io.snice.networking.app.impl;

import io.snice.networking.app.MessagePipe;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class MessagePipeTest {

    @Before
    public void setup() throws Exception {
    }

    @Test
    public void testBasicPipe() throws Exception {
        final var pipe = MessagePipe.match((String ctx, String v) -> v.startsWith("hello")).map(String::length);
        assertThat(pipe.apply("asdf", "hello world"), CoreMatchers.is(11));

        // note: the match is something you have to do "manually", only filter
        // will apply to values that is given to the actual pipe...
        assertThat(pipe.apply("asdf", "not hello world"), CoreMatchers.is(15));

        assertThat(pipe.test("", "not hello"), CoreMatchers.is(false));


        final var pipe2 = pipe.map(i -> "count " + i);
        assertThat(pipe.apply("asdf", "hello world"), CoreMatchers.is(11));
        assertThat(pipe2.apply("asdf", "hello world"), CoreMatchers.is("count 11"));
    }

    @Test
    public void testBranchingPipe() throws Exception {

    }
}
