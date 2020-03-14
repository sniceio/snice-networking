package io.snice.networking.codec.diameter.impl;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.diameter.DiameterAnswer;
import io.snice.networking.codec.diameter.DiameterHeader;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.api.ExperimentalResultCode;
import io.snice.networking.codec.diameter.avp.api.ResultCode;

import java.util.List;

public class DiameterAnswerBuilder extends DiameterMessageBuilder<DiameterAnswer> implements DiameterAnswer.Builder {

    public DiameterAnswerBuilder(final ResultCode resultCode) {
        super(resultCode);
    }

    public DiameterAnswerBuilder(final ExperimentalResultCode resultCode) {
        super(resultCode);
    }

    @Override
    protected DiameterAnswer internalBuild(final Buffer message, final DiameterHeader header, final List<FramedAvp> avps,
                                           final short indexOfOriginHost, final short indexOfOriginRealm,
                                           final short indexOfDestinationHost, final short indexOfDestinationRealm,
                                           final short indexResultCode, final short indexExperimentalResultCode) {
        return new ImmutableDiameterAnswer(message, header, avps, indexOfOriginHost, indexOfOriginRealm,
                indexOfDestinationHost, indexOfDestinationRealm, indexResultCode, indexExperimentalResultCode);
    }
}
