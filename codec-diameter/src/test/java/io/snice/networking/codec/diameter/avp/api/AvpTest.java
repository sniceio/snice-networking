package io.snice.networking.codec.diameter.avp.api;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.AvpHeader;
import io.snice.networking.codec.diameter.avp.type.DiameterType;
import io.snice.networking.codec.diameter.avp.type.Enumerated;
import io.snice.networking.codec.diameter.avp.type.Integer32;
import io.snice.networking.codec.diameter.avp.type.UTF8String;
import org.junit.Test;

import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AvpTest {

    @Test
    public void testBasicAvp() {
        final var avp = Avp.ofType(Integer32.class).withValue(Integer32.of(234)).withAvpCode(100).build();
        assertThat(avp.getData().toReadableBuffer().readInt(), is(234));
        // the total length for Integer32 AVPs is 12 or 16 if the vendor is is present.
        // in the above example, there is no vendor id so 12 it is...
        // and the length of the header without the vendor id is 8
        ensureAvp(avp, Integer32.of(234), 100L, 8, 12, false, false, Optional.empty());


        final var avp2 = Avp.ofType(UTF8String.class)
                .withValue(UTF8String.of("hello world"))
                .withAvpCode(123)
                .isMandatory()
                .withVendorId(123456L)
                .build();
        assertThat(avp2.getData().toString(), is("hello world"));
        final int expectedHeaderLength = 12; // because of the vendor id
        final int totalLength = expectedHeaderLength + "hello world".getBytes().length;
        ensureAvp(avp2, UTF8String.of("hello world"), 123L, expectedHeaderLength, totalLength, true, false, Optional.of(123456L));
    }

    @Test
    public void testEnumeratedAvp() {
        // TODO: need to come up with some better way of doing this holder. looks like
        // we may need it for all enum types so parameterize it?
        final Enumerated<ResultCode.Code> value = new EnumeratedHolder(ResultCode.Code.DIAMETER_SUCCESS_2001);

        final var avp = Avp.ofType(Enumerated.class)
                .withValue(value)
                .withAvpCode(Integer.MAX_VALUE).build();
        assertThat(avp.getData().toReadableBuffer().readInt(), is(ResultCode.Code.DIAMETER_SUCCESS_2001.getCode()));
        ensureAvp(avp, value, Integer.MAX_VALUE, 8, 12, false, false, Optional.empty());
    }

    class EnumeratedHolder implements Enumerated<ResultCode.Code> {

        private final int code;
        private final Optional<ResultCode.Code> e;

        private EnumeratedHolder(final ResultCode.Code code) {
            this(code.getCode(), Optional.of(code));
        }

        private EnumeratedHolder(final int code, final Optional<ResultCode.Code> e) {
            this.code = code;
            this.e = e;
        }

        @Override
        public Optional<ResultCode.Code> getAsEnum() {
            return e;
        }

        @Override
        public int getValue() {
            return code;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final EnumeratedHolder that = (EnumeratedHolder) o;
            return code == that.code;
        }

        @Override
        public int hashCode() {
            return Objects.hash(code);
        }
    }

    private void ensureAvp(final Avp avp,
                           final DiameterType expectedValue,
                           final long expectedCode,
                           final int expectedHeaderLength,
                           final int expectedTotalLength,
                           final boolean expectedMandatory,
                           final boolean expectedProtected,
                           final Optional<Long> expectedVendorId) {

        assertThat(avp.getValue(), is(expectedValue));
        assertThat(avp.getCode(), is(expectedCode));
        assertThat(avp.getLength(), is(expectedTotalLength));

        // frame it back out again to ensure all is well
        final Buffer rawAvpHeaderBuffer = avp.getHeader().getBuffer();
        final var h = AvpHeader.frame(rawAvpHeaderBuffer.toReadableBuffer());
        ensureAvpHeader(h, expectedCode, expectedHeaderLength, expectedTotalLength, expectedMandatory, expectedProtected, expectedVendorId);
    }

    private void ensureAvpHeader(final AvpHeader actual,
                                 final long expectedCode,
                                 final int expectedHeaderLength,
                                 final int expectedTotalLength,
                                 final boolean expectedMandatory,
                                 final boolean expectedProtected,
                                 final Optional<Long> expectedVendorId) {
        assertThat(actual.getCode(), is(expectedCode));
        assertThat(actual.getHeaderLength(), is(expectedHeaderLength));
        assertThat(actual.getLength(), is(expectedTotalLength));
        assertThat(actual.isMandatory(), is(expectedMandatory));
        assertThat(actual.isProtected(), is(expectedProtected));
        assertThat(actual.getVendorId(), is(expectedVendorId));
        assertThat(actual.isVendorSpecific(), is(expectedVendorId.isPresent()));
    }
}
