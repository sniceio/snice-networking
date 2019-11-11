package io.snice.networking.codec.diameter;

import io.snice.buffer.Buffers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class TransactionIdentifierTest extends DiameterTestBase {

    @Test
    public void testHashCodeAndEquals() {
        ensureHashCodeAndEquals(21345L, "whatever.mnc.mcc.blah.3gppnetwork.org");
        ensureHashCodeAndEquals(-1, "whatever.mnc.mcc.blah.3gppnetwork.org");

        ensureHashCodeAndEquals(1234L, "hello", 1234L, "nope", false);
        ensureHashCodeAndEquals(1234L, "hello", 7890L, "hello", false);
    }

    private void ensureHashCodeAndEquals(final long endToEndId, final String originHost) {
        ensureHashCodeAndEquals(endToEndId, originHost, endToEndId, originHost, true);
    }

    private void ensureHashCodeAndEquals(final long endToEndIdOne,
                                         final String originHostOne,
                                         final long endToEndIdTwo,
                                         final String originHostTwo,
                                         final boolean isEqual) {
        final var h1 = createOriginHost(Buffers.wrap(originHostOne), true);
        final var h2 = createOriginHost(Buffers.wrap(originHostTwo), true);

        final var t1 = TransactionIdentifier.of(endToEndIdOne, h1);
        final var t2 = TransactionIdentifier.of(endToEndIdTwo, h2);

        ensureHashCodeAndEquals(t1, t2, isEqual);
    }


    private void ensureHashCodeAndEquals(final TransactionIdentifier t1, final TransactionIdentifier t2, final boolean isEqual) {
        if (isEqual) {
            assertThat(t1.hashCode(), is(t2.hashCode()));
            assertThat(t1, is(t2));
        } else {
            assertThat(t1.hashCode(), not(t2.hashCode()));
            assertThat(t1, not(t2));
        }
    }

}
