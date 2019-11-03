package io.snice.networking.codec.diameter;

import io.snice.buffer.ReadableBuffer;
import io.snice.networking.codec.Framer;
import io.snice.networking.codec.SerializationFactory;
import io.snice.networking.codec.diameter.impl.DiameterParser;

import java.util.Optional;

public class DiameterSerializationFactory implements SerializationFactory<DiameterMessage> {

    private static final Framer<DiameterMessage> framer = buffer -> Optional.of(DiameterMessage.frame(buffer));
    private static final Framer<DiameterMessage> streamFramer = new StreamBasedDiameterFramer();

    @Override
    public Framer<DiameterMessage> getFramer() {
        return streamFramer;
    }

    /**
     * Framer for stream based protocols, such as TCP.
     * <p>
     * Diameter is quite simple to frame even for a stream based protocol since you only need to read
     * the header to know how many more bytes you need to wait for before you can frame it all.
     */
    private static final class StreamBasedDiameterFramer implements Framer<DiameterMessage> {

        @Override
        public Optional<DiameterMessage> frame(final ReadableBuffer buffer) {
            if (DiameterParser.canFrameMessage(buffer)) {
                return Optional.of(DiameterMessage.frame(buffer));
            }

            return Optional.empty();
        }
    }


}