package io.snice.networking.codec.diameter.impl;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.diameter.DiameterAnswer;
import io.snice.networking.codec.diameter.DiameterHeader;
import io.snice.networking.codec.diameter.DiameterParseException;
import io.snice.networking.codec.diameter.DiameterRequest;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.api.ResultCode;

import java.util.List;

public class ImmutableDiameterRequest extends ImmutableDiameterMessage implements DiameterRequest {

    public ImmutableDiameterRequest(final Buffer raw,
                                    final DiameterHeader header,
                                    final List<FramedAvp> avps,
                                    final short indexOriginHost,
                                    final short indexOriginRealm) {
        super(raw, header, avps, indexOriginHost, indexOriginRealm);
    }

    @Override
    public final boolean isRequest() {
        return true;
    }

    @Override
    public final DiameterRequest toRequest() {
        return this;
    }

    @Override
    public DiameterAnswer.Builder createAnswer(final ResultCode resultCode) throws DiameterParseException, ClassCastException {
        final var builder = ImmutableDiameterAnswer.withResultCode(resultCode);
        builder.withOriginHost(getOriginHost());
        builder.withOriginRealm(getOriginRealm());
        return builder;
    }

    @Override
    public DiameterRequest.Builder copy() {
        throw new RuntimeException("Not implemented just yet");
    }
}
