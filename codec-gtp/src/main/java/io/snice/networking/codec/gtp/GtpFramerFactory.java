package io.snice.networking.codec.gtp;

import io.snice.networking.codec.Framer;
import io.snice.networking.codec.FramerFactory;

import java.util.Optional;

public class GtpFramerFactory implements FramerFactory<GtpMessage> {

    private static Framer<GtpMessage> framer = buffer -> Optional.of(GtpMessage.frame(buffer));

    @Override
    public Framer<GtpMessage> getFramer() {
        return framer;
    }
}
