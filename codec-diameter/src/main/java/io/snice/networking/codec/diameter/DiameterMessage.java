package io.snice.networking.codec.diameter;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.networking.codec.diameter.avp.Avp;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.api.DestinationHost;
import io.snice.networking.codec.diameter.avp.api.DestinationRealm;
import io.snice.networking.codec.diameter.avp.api.OriginHost;
import io.snice.networking.codec.diameter.avp.api.OriginRealm;
import io.snice.networking.codec.diameter.avp.api.ResultCode;
import io.snice.networking.codec.diameter.impl.DiameterParser;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author jonas@jonasborjesson.com
 */
public interface DiameterMessage extends Cloneable {

    DiameterHeader getHeader();

    List<FramedAvp> getAllAvps();

    DiameterMessage clone();

    Optional<FramedAvp> getAvp(long code);

    Builder<? extends DiameterMessage> copy();

    default boolean isRequest() {
        return false;
    }

    default DiameterRequest toRequest() {
        throw new ClassCastException("Unable to cast this " + getClass().getName()
                + " into a " + DiameterRequest.class.getName());
    }

    default boolean isAnswer() {
        return false;
    }

    default DiameterAnswer toAnswer() {
        throw new ClassCastException("Unable to cast this " + getClass().getName()
                + " into a " + DiameterAnswer.class.getName());
    }

    default boolean isULR() {
        final var header = getHeader();
        return header.isRequest() && header.getCommandCode() == 316;
    }

    default boolean isULA() {
        final var header = getHeader();
        return header.isAnswer() && header.getCommandCode() == 316;
    }

    default boolean isCER() {
        final var header = getHeader();
        return header.isRequest() && header.getCommandCode() == 257;
    }

    default boolean isCEA() {
        final var header = getHeader();
        return header.isAnswer() && header.getCommandCode() == 257;
    }

    /**
     * Get the entire {@link DiameterMessage} as a {@link Buffer}, which you then can use
     * to e.g. write to network socket.
     */
    Buffer getBuffer();

    /**
     * The {@link OriginHost} MUST be present in all diameter messages.
     */
    OriginHost getOriginHost();

    /**
     * The {@link OriginRealm} MUST be present in all diameter messages.
     */
    OriginRealm getOriginRealm();

    static DiameterMessage frame(final Buffer buffer) {
        return DiameterParser.frame(buffer.toReadableBuffer());
    }

    static DiameterMessage frame(final ReadableBuffer buffer) {
        return DiameterParser.frame(buffer);
    }

    /**
     * Create a new answer based on this {@link DiameterMessage}. If this
     * {@link DiameterMessage} is not a {@link DiameterRequest} then a
     * {@link ClassCastException} will be thrown. Only the mandatory {@link Avp}s
     * from the {@link DiameterRequest} are copied. Those mandatory AVPs are:
     * <ul>
     * <li>TODO</li>
     * </ul>
     *
     * @param resultCode
     * @return
     * @throws DiameterParseException in case anything goes wrong when parsing out AVPs from the
     *                                {@link DiameterMessage}
     */
    default DiameterAnswer.Builder createAnswer(final ResultCode resultCode) throws DiameterParseException, ClassCastException {
        throw new ClassCastException("Unable to cast this " + getClass().getName()
                + " into a " + DiameterAnswer.class.getName());
    }

    interface Builder<T extends DiameterMessage> {

        default boolean isDiameterRequestBuilder() {
            return false;
        }

        default boolean isDiameterAnswerBuilder() {
            return false;
        }

        default DiameterMessage.Builder<DiameterRequest> toDiameterRequestBuilder() {
            throw new ClassCastException("Cannot cast " + getClass().getName() + " into a "
                    + DiameterRequest.class.getName() + " builder");
        }

        default DiameterMessage.Builder<DiameterAnswer> toDiameterResponseBuilder() {
            throw new ClassCastException("Cannot cast " + getClass().getName() + " into a "
                    + DiameterAnswer.class.getName() + " builder");
        }

        /**
         * Whenever an {@link Avp} is about to be pushed onto the new {@link DiameterMessage}
         * you have a chance to change the value of that AVP. You do so
         * by registering a function that accepts an {@link Avp} as an argument and that
         * returns a {@link Avp}, which is the AVP that will be pushed onto the new
         * {@link DiameterMessage}. If you do not want to include the AVP, then simply return
         * null and that AVP will be dropped.
         * <p>
         * If you wish to leave the AVP un-touched, then simply return it has is.
         *
         * @param f
         * @return
         * @throws IllegalStateException in case a function already had been registered with
         *                               this builder.
         */
        Builder<T> onAvp(Function<Avp, Avp> f) throws IllegalStateException;

        /**
         * Add the {@link Avp} to the list of AVPs already specified within this builder.
         * The {@link Avp} will be added last to the list of AVPs.
         *
         * @param avp
         * @return
         */
        Builder<T> withAvp(Avp avp);

        Builder<T> withOriginHost(OriginHost originHost);

        Builder<T> withOriginRealm(OriginRealm originHost);

        Builder<T> withDestinationHost(DestinationHost destHost);

        Builder<T> withDestinationRealm(DestinationRealm destRealm);

        /**
         * The length of the entire message must be encoded into the {@link DiameterHeader}
         * and as such, we need access to it within the {@link Builder} and we'll calculate
         * the correct length upon {@link #build()}.
         *
         * @param header
         */
        Builder<T> withDiameterHeader(final DiameterHeader.Builder header);

        T build();

        /**
         * After the {@link DiameterMessage} has been fully built and created, the "end result"
         * will be conveyed to the registered function. It is utterly important
         * that the function returns as quickly as possible since the build method
         * will not be able to return until the call to this function has been completed.
         *
         * @param f
         */
        Builder<T> onCommit(Consumer<DiameterMessage> f);

    }
}
