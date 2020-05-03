package io.snice.codecs.codec.diameter.impl;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.diameter.DiameterAnswer;
import io.snice.codecs.codec.diameter.DiameterHeader;
import io.snice.codecs.codec.diameter.avp.FramedAvp;
import io.snice.codecs.codec.diameter.avp.api.ExperimentalResultCode;
import io.snice.codecs.codec.diameter.avp.api.ResultCode;
import io.snice.functional.Either;

import java.util.List;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class ImmutableDiameterAnswer extends ImmutableDiameterMessage implements DiameterAnswer {

    public ImmutableDiameterAnswer(final Buffer raw,
                                   final DiameterHeader header,
                                   final List<FramedAvp> avps,
                                   final short indexOriginHost,
                                   final short indexOriginRealm,
                                   final short indexDestinationHost,
                                   final short indexDestinationRealm,
                                   final short indexResultCode,
                                   final short indexExperimentalCode) {
        super(raw, header, avps, indexOriginHost, indexOriginRealm, indexDestinationHost, indexDestinationRealm, indexResultCode, indexExperimentalCode);
    }

    @Override
    public final boolean isAnswer() {
        return true;
    }

    @Override
    public final DiameterAnswer toAnswer() {
        return this;
    }

    @Override
    public DiameterAnswer.Builder copy() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public Either<ExperimentalResultCode, ResultCode> getResultCode() {
        return getInternalResultCode();
    }

    public static DiameterAnswer.Builder withResultCode(final ResultCode resultCode) {
        assertNotNull(resultCode, "You must specify the result code");
        return new DiameterAnswerBuilder(resultCode);
    }

    public static DiameterAnswer.Builder withResultCode(final ExperimentalResultCode resultCode) {
        assertNotNull(resultCode, "You must specify the result code");
        return new DiameterAnswerBuilder(resultCode);
    }

}
