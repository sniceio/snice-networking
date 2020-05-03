package io.snice.codecs.codec.diameter.avp.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.WritableBuffer;
import io.snice.codecs.codec.diameter.avp.AvpHeader;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertArgument;

/**
 * @author jonas@jonasborjesson.com
 */
public class ImmutableAvpHeader implements AvpHeader {

    /**
     * just the SEPARATOR we use for the toString-method
     */
    private static final String SEPARATOR = ":";

    private final Buffer buffer;
    private final Optional<Long> vendorId;

    public ImmutableAvpHeader(final Buffer buffer, final Optional<Long> vendorId) {
        this.buffer = buffer;
        this.vendorId = vendorId;
    }

    public static Builder withCode(final long code) {
        assertArgument(code >= 0, "The code must be larger than zero");
        return new Builder(code);
    }

    @Override
    public int getHeaderLength() {
        // the AVP header length is always at least 8 bytes plus an additional 4 if
        // the optional vendor id is set.
        return vendorId.isPresent() ? 12 : 8;
    }

    @Override
    public Buffer getBuffer() {
        return buffer;
    }

    @Override
    public void writeTo(final WritableBuffer out) {
        buffer.writeTo(out);
    }

    @Override
    public long getCode() {
        return buffer.getUnsignedInt(0);
    }

    @Override
    public int getLength() {
        try {
            return buffer.getIntFromThreeOctets(5);
        } catch (final Exception e) {
            return -1;
        }
    }

    @Override
    public Optional<Long> getVendorId() {
        return vendorId;
    }

    @Override
    public boolean isVendorSpecific() {
        // 5th byte is the command flags
        return checkFirstFlag(buffer, 4);
    }

    @Override
    public boolean isMandatory() {
        return checkSecondFlag(buffer, 4);
    }

    @Override
    public boolean isProtected() {
        return checkThirdFlag(buffer, 4);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(getCode()).append(SEPARATOR);
        sb.append(isVendorSpecific() ? "V" : "v");
        sb.append(isMandatory() ? "M" : "m");
        sb.append(isProtected() ? "P" : "p");
        sb.append(SEPARATOR);
        sb.append(getLength());
        sb.append(SEPARATOR);
        vendorId.ifPresent(sb::append);
        sb.append(SEPARATOR);
        return sb.toString();
    }

    private static boolean checkFirstFlag(final Buffer buffer, final int index) {
        try {
            return (buffer.getByte(index) & 0b10000000) == 0b10000000;
        } catch (final IndexOutOfBoundsException e) {
            throw new RuntimeException("Unable to read byte from underlying buffer", e);
        }
    }

    private static boolean checkSecondFlag(final Buffer buffer, final int index) {
        try {
            return (buffer.getByte(index) & 0b01000000) == 0b01000000;
        } catch (final IndexOutOfBoundsException e) {
            throw new RuntimeException("Unable to read byte from underlying buffer", e);
        }
    }

    private static boolean checkThirdFlag(final Buffer buffer, final int index) {
        try {
            return (buffer.getByte(index) & 0b00100000) == 0b00100000;
        } catch (final IndexOutOfBoundsException e) {
            throw new RuntimeException("Unable to read byte from underlying buffer", e);
        }
    }

    public static class Builder implements AvpHeader.Builder {

        /**
         * The vast majority of all {@link AvpHeader}s are 8 bytes long so let's assume
         * that. Only if the vendor id is set, the header grows another 4 bytes and
         * if that happens, we'll rebuild it when we build the header...
         */
        private final long code;
        private boolean isMandatory;
        private boolean isProtected;
        private long vendorId = -1;

        private Builder(final long code) {
            this.code = code;
        }

        @Override
        public AvpHeader.Builder isMandatory() {
            this.isMandatory = true;
            return this;
        }

        @Override
        public AvpHeader.Builder isProtected() {
            this.isProtected = true;
            return this;
        }

        @Override
        public AvpHeader.Builder withVendorId(final long vendorId) {
            this.vendorId = vendorId;
            return this;
        }

        @Override
        public AvpHeader build() {
            final WritableBuffer buffer;
            final Optional<Long> vendorIdOptional;
            if (vendorId >= 0) {
                buffer = WritableBuffer.of(new byte[12]);
                buffer.setWriterIndex(12); // because we are only using setXXX as opposed to writeXXXX
                buffer.setBit7(4, true);
                buffer.setUnsignedInt(8, vendorId);
                vendorIdOptional = Optional.of(vendorId);
            } else {
                buffer = WritableBuffer.of(new byte[8]);
                buffer.setWriterIndex(8);
                vendorIdOptional = Optional.empty();
            }
            buffer.setUnsignedInt(0, code);
            buffer.setBit6(4, isMandatory);
            buffer.setBit5(4, isProtected);

            return new ImmutableAvpHeader(buffer.build(), vendorIdOptional);
        }
    }

}
