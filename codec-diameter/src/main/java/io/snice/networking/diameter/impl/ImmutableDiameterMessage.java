package io.snice.networking.diameter.impl;

import io.snice.buffer.Buffer;
import io.snice.networking.diameter.DiameterHeader;
import io.snice.networking.diameter.DiameterMessage;
import io.snice.networking.diameter.avp.FramedAvp;
import io.snice.networking.diameter.avp.api.OriginHost;
import io.snice.networking.diameter.avp.api.OriginRealm;

import java.util.List;

public class ImmutableDiameterMessage implements DiameterMessage {

    /**
     * The full raw diameter message.
     */
    private final Buffer raw;

    private final DiameterHeader header;
    private final List<FramedAvp> avps;

    public ImmutableDiameterMessage(final Buffer raw, final DiameterHeader header, final List<FramedAvp> avps) {
        this.raw = raw;
        this.header = header;
        this.avps = avps;
    }

    @Override
    public DiameterHeader getHeader() {
        return header;
    }

    @Override
    public List<FramedAvp> getAllAvps() {
        return avps;
    }

    /**
     * This class is immutable and as such, when cloning, you'll just get back the same
     * regference again.
     *
     * @return
     */
    @Override
    public DiameterMessage clone() {
        return this;
    }

    @Override
    public OriginHost getOriginHost() {
        return null;
    }

    @Override
    public OriginRealm getOriginRealm() {
        return null;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(header.toString());
        sb.append(", AVP Count: ").append(avps.size());
        return sb.toString();
    }
}
