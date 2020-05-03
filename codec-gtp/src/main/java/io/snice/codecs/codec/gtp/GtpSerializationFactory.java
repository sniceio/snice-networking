package io.snice.codecs.codec.gtp;

import io.snice.codecs.codec.Framer;
import io.snice.codecs.codec.SerializationFactory;

import java.util.Optional;

public class GtpSerializationFactory implements SerializationFactory<GtpMessage> {

    private static final Framer<GtpMessage> framer = buffer -> Optional.of(GtpMessage.frame(buffer));

    @Override
    public Framer<GtpMessage> getFramer() {
        return framer;
    }
}
