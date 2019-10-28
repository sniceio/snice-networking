package io.snice.networking.codec.diameter.impl;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.diameter.DiameterHeader;
import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.api.OriginHost;
import io.snice.networking.codec.diameter.avp.api.OriginRealm;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class ImmutableDiameterMessage implements DiameterMessage {

    /**
     * The full raw diameter message.
     */
    private final Buffer raw;

    private final DiameterHeader header;
    private final List<FramedAvp> avps;

    private final short indexOrigHost;
    private final short indexOrigRealm;

    public ImmutableDiameterMessage(final Buffer raw,
                                    final DiameterHeader header,
                                    final List<FramedAvp> avps,
                                    final short indexOrigHost,
                                    final short indexOrigRealm) {
        this.raw = raw;
        this.header = header;
        this.avps = Collections.unmodifiableList(avps);
        this.indexOrigHost = indexOrigHost;
        this.indexOrigRealm = indexOrigRealm;
    }

    @Override
    public Optional<FramedAvp> getAvp(final long code) {
        return avps.stream().filter(avp -> avp.getCode() == code).findFirst();
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
        return avps.get(indexOrigHost).parse().toOriginHost();
    }

    @Override
    public OriginRealm getOriginRealm() {
        return avps.get(indexOrigRealm).parse().toOriginRealm();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(header.toString());
        sb.append(", AVP Count: ").append(avps.size());
        return sb.toString();
    }
}
