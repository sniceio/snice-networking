package io.snice.networking.codec.diameter;

import io.snice.networking.codec.diameter.impl.DiameterRequestBuilder;

/**
 * @author jonas@jonasborjesson.com
 */
public interface DiameterRequest extends DiameterMessage {

    static Builder createCER() {
        return DiameterRequestBuilder.createCER();
    }


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
