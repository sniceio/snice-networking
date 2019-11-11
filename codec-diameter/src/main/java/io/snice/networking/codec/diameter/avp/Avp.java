package io.snice.networking.codec.diameter.avp;

import io.snice.buffer.Buffer;
import io.snice.buffer.WritableBuffer;
import io.snice.networking.codec.diameter.avp.api.OriginHost;
import io.snice.networking.codec.diameter.avp.api.OriginRealm;
import io.snice.networking.codec.diameter.avp.impl.ImmutableAvp;
import io.snice.networking.codec.diameter.avp.impl.ImmutableAvpHeader;
import io.snice.networking.codec.diameter.avp.impl.ImmutableFramedAvp;
import io.snice.networking.codec.diameter.avp.type.DiameterType;
import io.snice.networking.codec.diameter.avp.type.Enumerated;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * The difference between this {@link Avp} and the raw version, {@link FramedAvp} is that
 * this one has been fully parsed so that we know what type it is etc. Quite often, you
 * don't want to work with every AVP in a message and as such, we shouldn't waste time to
 * ensure them all fully, which this allows you to do. So the normal flow is that most
 * of your code will work with the FramedAvp because you don't care what it is, however, for
 * certain AVPs you do want to convert them to their real fully parsed versions, since it
 * is simply just easier to work with.
 */
public interface Avp<T extends DiameterType> extends FramedAvp {

    String CANNOT_CAST_AVP_OF_TYPE = "Cannot cast AVP of type ";

    T getValue();

    /**
     * Write just the value (so not the AVP header) to the specified buffer.
     * If you wish to write out the entire {@link Avp} then use the method
     * {@link FramedAvp#writeTo(WritableBuffer)}.
     *
     * @param buffer
     */
    default void writeValue(final WritableBuffer buffer) {
        throw new RuntimeException("Not yet implemented for " + getClass().getName());
    }

    static <T extends DiameterType> ValueStep<T> ofType(final Class<T> type) {
        assertNotNull(type);
        return value -> {
            assertNotNull(value);
            return code -> new DefaultBuilder<>(value, code);
        };
    }

    interface ValueStep<T extends DiameterType> {
        AvpCodeStep<T> withValue(T value);
    }

    interface AvpCodeStep<T extends DiameterType> {
        Builder<T> withAvpCode(long code);
    }


    interface Builder<T extends DiameterType> {

        /**
         * Set the 'M' bit, which indicates that this {@link Avp} is mandatory.
         * <p>
         * Default value is false.
         */
        default Builder<T> isMandatory() {
            return isMandatory(true);
        }

        /**
         * Set the value of the 'M' bit, which indicates whether this {@link Avp} is mandatory
         * or not.
         * <p>
         * Default value is false.
         */
        Builder<T> isMandatory(boolean value);

        /**
         * Set the 'P' big, which indicates that this {@link Avp} is protected.
         */
        default Builder<T> isProtected() {
            return isProtected(true);
        }

        Builder<T> isProtected(boolean value);

        /**
         * Set the optional vendor id. If set, the 'V' bit will also
         * be set, indicating that this {@link AvpHeader} has the vendor id
         * set.
         *
         * @param vendorId
         */
        Builder<T> withVendorId(long vendorId);

        Avp<T> build();
    }

    /**
     * Check if this AVP is an enumerated AVP.
     *
     * @return
     */
    default boolean isEnumerated() {
        return false;
    }

    default <E extends Enum<E>> Avp<Enumerated<E>> toEnumerated() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + this.getClass().getName() + " into a " + Enumerated.class.getName());
    }

    default boolean isOriginHost() {
        return false;
    }

    default OriginHost toOriginHost() {
        throw new ClassCastException(CANNOT_CAST_AVP_OF_TYPE + getClass().getName()
                + " to type " + OriginHost.class.getName());
    }

    default boolean isOriginRealm() {
        return false;
    }

    default OriginRealm toOriginRealm() {
        throw new ClassCastException(CANNOT_CAST_AVP_OF_TYPE + getClass().getName()
                + " to type " + OriginRealm.class.getName());
    }

    class DefaultBuilder<T extends DiameterType> implements Builder<T> {

        private final T value;
        private final long avpCode;
        private boolean isMandatory;
        private boolean isProtected;
        private long vendorId = -1;

        private DefaultBuilder(final T value, final long avpCode) {
            this.value = value;
            this.avpCode = avpCode;
        }

        @Override
        public Builder<T> isMandatory(final boolean value) {
            this.isMandatory = value;
            return this;
        }

        @Override
        public Builder<T> isProtected(final boolean value) {
            this.isProtected = value;
            return this;
        }

        @Override
        public Builder<T> withVendorId(final long vendorId) {
            this.vendorId = vendorId;
            return this;
        }

        @Override
        public Avp<T> build() {
            final WritableBuffer writable;
            final Optional<Long> vendorIdOptional;

            final int headerLength = vendorId == -1 ? 8 : 12;
            final int totalLength = headerLength + value.size();
            writable = WritableBuffer.of(new byte[totalLength]);
            writable.setWriterIndex(headerLength); // because of the header we only use setXXX as opposed to writes

            if (vendorId >= 0) {
                writable.setBit7(4, true);
                writable.setUnsignedInt(8, vendorId);
                vendorIdOptional = Optional.of(vendorId);
            } else {
                vendorIdOptional = Optional.empty();
            }
            writable.setUnsignedInt(0, avpCode);
            writable.setBit6(4, isMandatory);
            writable.setBit5(4, isProtected);
            writable.setThreeOctetInt(5, totalLength);

            value.writeValue(writable);
            final Buffer buffer = writable.build();
            final Buffer header = buffer.slice(headerLength);
            final Buffer data = buffer.slice(headerLength, totalLength);

            final AvpHeader avpHeader = new ImmutableAvpHeader(header, vendorIdOptional);
            final FramedAvp framedAvp = new ImmutableFramedAvp(avpHeader, data);
            return new ImmutableAvp(framedAvp, value);
        }
    }
}
