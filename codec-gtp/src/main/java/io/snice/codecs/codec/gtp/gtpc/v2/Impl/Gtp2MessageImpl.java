package io.snice.codecs.codec.gtp.gtpc.v2.Impl;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.codecs.codec.gtp.GtpHeader;
import io.snice.codecs.codec.gtp.UnknownGtp2MessageTypeException;
import io.snice.codecs.codec.gtp.gtpc.InfoElement;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Header;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2InfoElementType;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2MessageType;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.TypeLengthInstanceValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class Gtp2MessageImpl implements Gtp2Message {

    /**
     * Map that contains all the builders for the existing GTPv2 message types. The default value of the map
     * is just so that it is way larger than potentially necessary because we want a good spread of the
     * buckets and the little memory we waste is just fine since:
     * 1. Memory is cheap
     * 2. It's only a single map for the app (well, per class loader I guess)
     */
    private static final Map<Gtp2MessageType, Function<Gtp2Header, Gtp2MesssageBuilder<? extends Gtp2Message>>> builders
            = new HashMap<>(200);

    static {
        builders.put(Gtp2MessageType.CREATE_SESSION_REQUEST, CreateSessionRequest::from);
    }

    private final Gtp2Header header;
    private final Buffer body;
    protected final List<TypeLengthInstanceValue> values;
    private final Gtp2MessageType type;

    private static Gtp2MesssageBuilder<? extends Gtp2Message> ensureBuilder(final Gtp2MessageType type, final Gtp2Header header) {
        final Function<Gtp2Header, Gtp2MesssageBuilder<?>> f = builders.get(type);
        if (f != null) {
            return f.apply(header);
        }

        return null;
    }

    @Override
    public Optional<? extends TypeLengthInstanceValue> getInformationElement(final Gtp2InfoElementType type) {
        assertNotNull(type, "The GTPv2 Information Element type cannot be null");
        return values.stream().filter(tliv -> type.getType() == tliv.getType()).findFirst();
    }

    public static Gtp2Message frame(final Gtp2Header header, final ReadableBuffer buffer) {
        assertNotNull(header, "The GTPv2 header cannot be null");
        assertNotNull(buffer, "The buffer cannot be null");
        final Buffer body = buffer.readBytes(header.getBodyLength());

        final Gtp2MessageType type = Gtp2MessageType.lookup(header.getMessageTypeDecimal());
        if (type == null) {
            throw new UnknownGtp2MessageTypeException(header.getMessageTypeDecimal());
        }

        final Gtp2MesssageBuilder<?> builder = ensureBuilder(type, header);
        builder.withBody(body);
        // return new Gtp2MessageImpl(type, header, body, Collections.unmodifiableList(tlivs));
        return builder.build();
    }

    @Override
    public Gtp2MessageType getType() {
        return type;
    }

    protected Gtp2MessageImpl(final Gtp2MessageType type, final Gtp2Header header, final Buffer body, final List<TypeLengthInstanceValue> values) {
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
