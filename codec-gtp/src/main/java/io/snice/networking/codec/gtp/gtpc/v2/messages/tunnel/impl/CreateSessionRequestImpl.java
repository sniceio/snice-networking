package io.snice.networking.codec.gtp.gtpc.v2.messages.tunnel.impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.networking.codec.gtp.gtpc.v2.Gtp2Header;
import io.snice.networking.codec.gtp.gtpc.v2.Gtp2InfoElementType;
import io.snice.networking.codec.gtp.gtpc.v2.Gtp2MessageType;
import io.snice.networking.codec.gtp.gtpc.v2.Impl.Gtp2MessageImpl;
import io.snice.networking.codec.gtp.gtpc.v2.Impl.Gtp2MesssageBuilder;
import io.snice.networking.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.networking.codec.gtp.gtpc.v2.tliv.IMSI;
import io.snice.networking.codec.gtp.gtpc.v2.tliv.TypeLengthInstanceValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class CreateSessionRequestImpl extends Gtp2MessageImpl implements CreateSessionRequest {

    private final int imsiIndex;

    public static Gtp2MesssageBuilder<CreateSessionRequest> from(final Gtp2Header header) {
        assertNotNull(header, "The GTPv2 Header cannot be null");
        return new CreateSessionRequestBuilder(header);
    }

    @Override
    public Optional<IMSI> getImsi() {
        return imsiIndex == -1 ? Optional.empty() : Optional.of(values.get(imsiIndex).ensure().toIMSI());
    }

    private CreateSessionRequestImpl(final Gtp2Header header, final Buffer body,
                                     final List<TypeLengthInstanceValue> values,
                                     final int imsiIndex) {
        super(Gtp2MessageType.CREATE_SESSION_REQUEST, header, body, values);
        this.imsiIndex = imsiIndex;
    }

    private static class CreateSessionRequestBuilder implements Gtp2MesssageBuilder<CreateSessionRequest> {

        private final List<TypeLengthInstanceValue> tlivs = new ArrayList<>();
        private final Gtp2Header header;
        private Buffer body;
        private int imsiIndex = -1;

        private CreateSessionRequestBuilder(final Gtp2Header header) {
            this.header = header;
        }

        @Override
        public Gtp2MesssageBuilder<CreateSessionRequest> withBody(final Buffer body) {
            this.body = body;
            final ReadableBuffer values = body.toReadableBuffer();
            int count = 0;
            while (values.hasReadableBytes()) {
                final TypeLengthInstanceValue tliv = TypeLengthInstanceValue.frame(values);
                if (tliv.getType() == Gtp2InfoElementType.IMSI.getType()) {
                    tlivs.add(tliv.ensure());
                    imsiIndex = count;
                } else {
                    tlivs.add(tliv);
                }
                ++count;
            }
            return this;
        }

        @Override
        public CreateSessionRequest build() {
            return new CreateSessionRequestImpl(header, body, Collections.unmodifiableList(tlivs), imsiIndex);
        }
    }

}
