package io.snice.networking.codec.diameter;

import io.snice.networking.codec.diameter.avp.api.ResultCode;
import io.snice.networking.codec.diameter.impl.ImmutableDiameterAnswer;

/**
 * @author jonas@jonasborjesson.com
 */
public interface DiameterAnswer extends DiameterMessage {

    ResultCode getResultCode();

    static Builder withResultCode(final ResultCode resultCode) {
        return ImmutableDiameterAnswer.withResultCode(resultCode);
    }

    interface Builder extends DiameterMessage.Builder<DiameterAnswer> {

    }
}
