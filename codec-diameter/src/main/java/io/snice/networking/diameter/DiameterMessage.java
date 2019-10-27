package io.snice.networking.diameter;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;
import io.snice.networking.diameter.avp.FramedAvp;
import io.snice.networking.diameter.avp.api.OriginHost;
import io.snice.networking.diameter.avp.api.OriginRealm;
import io.snice.networking.diameter.impl.DiameterParser;

import java.io.IOException;
import java.util.List;

/**
 * @author jonas@jonasborjesson.com
 */
public interface DiameterMessage extends Cloneable {

    DiameterHeader getHeader();

    List<FramedAvp> getAllAvps();

    DiameterMessage clone();

    /**
     * The {@link OriginHost} MUST be present in all diameter messages.
     */
    OriginHost getOriginHost();

    /**
     * The {@link OriginRealm} MUST be present in all diameter messages.
     */
    OriginRealm getOriginRealm();

    static DiameterMessage frame(final Buffer buffer) {
        return DiameterParser.frame(buffer.toReadableBuffer());
    }

    static DiameterMessage frame(final ReadableBuffer buffer) throws IOException {
        return DiameterParser.frame(buffer);
    }
}
