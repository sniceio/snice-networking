package io.snice.networking.diameter.peer.impl;

import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.networking.diameter.tx.Transaction;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class TransactionTest extends PeerTestBase {

    private Transaction.Builder builder;
    private DiameterRequest req;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        req = someUlr();
        defaultPeer.establishPeer();
        builder = defaultPeer.createNewTransaction(req);
    }

    @Test
    public void testSendInTransaction() {
        final var t = builder.start();
        assertThat(t, notNullValue());
    }

    @Test
    public void testWithAppData() {
        final var t = builder.withApplicationData("hello").start();
        assertThat(t.getApplicationData(), is(Optional.of("hello")));
    }

    @Test
    public void testWithOutAppData() {
        assertThat(builder.start().getApplicationData(), is(Optional.empty()));
    }

}