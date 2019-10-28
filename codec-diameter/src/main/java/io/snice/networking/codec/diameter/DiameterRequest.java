package io.snice.networking.codec.diameter;

/**
 * @author jonas@jonasborjesson.com
 */
public interface DiameterRequest extends DiameterMessage {


    interface Builder extends DiameterMessage.Builder<DiameterRequest> {

        @Override
        default boolean isDiameterRequestBuilder() {
            return true;
        }

        @Override
        default DiameterMessage.Builder<DiameterRequest> toDiameterRequestBuilder() {
            return this;
        }

    }
}
