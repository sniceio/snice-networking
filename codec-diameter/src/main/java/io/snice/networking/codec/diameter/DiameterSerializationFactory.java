package io.snice.networking.codec.diameter;

import io.snice.networking.codec.Framer;
import io.snice.networking.codec.SerializationFactory;

import java.util.Optional;

public class DiameterSerializationFactory implements SerializationFactory<DiameterMessage> {

    private static final Framer<DiameterMessage> framer = buffer -> Optional.of(DiameterMessage.frame(buffer));

    @Override
    public Framer<DiameterMessage> getFramer() {
        return framer;
    }


}