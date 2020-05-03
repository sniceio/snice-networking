package io.snice.codecs.codec.diameter;

import io.snice.codecs.codec.diameter.avp.api.ExperimentalResultCode;
import io.snice.codecs.codec.diameter.avp.api.ResultCode;
import io.snice.codecs.codec.diameter.impl.ImmutableDiameterAnswer;
import io.snice.functional.Either;

/**
 * @author jonas@jonasborjesson.com
 */
public interface DiameterAnswer extends DiameterMessage {

    /**
     * Every {@link DiameterAnswer} will have a result code. Either is is the standard
     * {@link ResultCode} or it will be a {@link ExperimentalResultCode}.
     *
     * @return
     */
    Either<ExperimentalResultCode, ResultCode> getResultCode();

    static Builder withResultCode(final ResultCode resultCode) {
        return ImmutableDiameterAnswer.withResultCode(resultCode);
    }

    interface Builder extends DiameterMessage.Builder<DiameterAnswer> {

    }
}
