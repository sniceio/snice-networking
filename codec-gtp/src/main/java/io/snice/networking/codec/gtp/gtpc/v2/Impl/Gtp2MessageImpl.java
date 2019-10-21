package io.snice.networking.codec.gtp.gtpc.v2.Impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.networking.codec.gtp.GtpHeader;
import io.snice.networking.codec.gtp.UnknownGtp2MessageTypeException;
import io.snice.networking.codec.gtp.gtpc.InfoElement;
import io.snice.networking.codec.gtp.gtpc.v2.Gtp2Header;
import io.snice.networking.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.networking.codec.gtp.gtpc.v2.Gtp2MessageType;
import io.snice.networking.codec.gtp.gtpc.v2.tliv.IMSI;
import io.snice.networking.codec.gtp.gtpc.v2.tliv.TypeLengthInstanceValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class Gtp2MessageImpl implements Gtp2Message {

    private final Gtp2Header header;
    private final Buffer body;
    private final List<TypeLengthInstanceValue> values;
    private final Gtp2MessageType type;

    public static Gtp2Message frame(final Gtp2Header header, final ReadableBuffer buffer) {
        assertNotNull(header, "The GTPv2 header cannot be null");
        assertNotNull(buffer, "The buffer cannot be null");
        final Buffer body = buffer.readBytes(header.getBodyLength());

        final ReadableBuffer values = body.toReadableBuffer();
        final List<TypeLengthInstanceValue> tlivs = new ArrayList<>(); // TODO: what's a good default value?
        while (values.hasReadableBytes()) {
            final TypeLengthInstanceValue tliv = TypeLengthInstanceValue.frame(values);
            if (tliv.isIMSI()) {
                final IMSI imsi = tliv.ensure().toIMSI();
                System.err.println(imsi);
            }
            tlivs.add(tliv);
        }

        final Gtp2MessageType type = Gtp2MessageType.lookup(header.getMessageTypeDecimal());
        if (type == null) {
            throw new UnknownGtp2MessageTypeException(header.getMessageTypeDecimal());
        }
        return new Gtp2MessageImpl(type, header, body, Collections.unmodifiableList(tlivs));
    }

    @Override
    public Gtp2MessageType getType() {
        return type;
    }

    private Gtp2MessageImpl(final Gtp2MessageType type, final Gtp2Header header, final Buffer body, final List<TypeLengthInstanceValue> values) {
        this.header = header;
        this.body = body;
        this.values = values;
        this.type = type;
    }

    @Override
    public GtpHeader getHeader() {
        return header;
    }

    @Override
    public List<? extends InfoElement> getInfoElements() {
        return values;
    }
}
