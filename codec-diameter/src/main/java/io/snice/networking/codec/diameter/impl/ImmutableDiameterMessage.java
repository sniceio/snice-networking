package io.snice.networking.codec.diameter.impl;

import io.snice.buffer.Buffer;
import io.snice.networking.codec.diameter.DiameterHeader;
import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.avp.FramedAvp;
import io.snice.networking.codec.diameter.avp.api.DestinationHost;
import io.snice.networking.codec.diameter.avp.api.DestinationRealm;
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

    protected final DiameterHeader header;
    private final List<FramedAvp> avps;

    private final short indexOrigHost;
    private final short indexOrigRealm;

    private final short indexDestHost;
    private final short indexDestRealm;

    public ImmutableDiameterMessage(final Buffer raw,
                                    final DiameterHeader header,
                                    final List<FramedAvp> avps,
                                    final short indexOrigHost,
                                    final short indexOrigRealm,
                                    final short indexDestHost,
                                    final short indexDestRealm) {
        this.raw = raw;
        this.header = header;
        this.avps = Collections.unmodifiableList(avps);
        this.indexOrigHost = indexOrigHost;
        this.indexOrigRealm = indexOrigRealm;
        this.indexDestHost = indexDestHost;
        this.indexDestRealm = indexDestRealm;
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

    @Override
    public Buffer getBuffer() {
        return raw;
    }
    /**
     * This class is immutable and as such, when cloning, you'll just get back the same
     * reference again.
     *
     * @return
     */
    @Override
    public DiameterMessage clone() {
        return this;
    }

    @Override
    public OriginHost getOriginHost() {
        return avps.get(indexOrigHost).ensure().toOriginHost();
    }

    @Override
    public OriginRealm getOriginRealm() {
        return avps.get(indexOrigRealm).ensure().toOriginRealm();
    }

    public Optional<DestinationRealm> getDestinationRealm() {
        if (indexDestRealm == -1) {
            return Optional.empty();
        }

        return Optional.of(avps.get(indexDestRealm).ensure().toDestinationRealm());
    }

    public Optional<DestinationHost> getDestinationHost() {
        if (indexDestHost == -1) {
            return Optional.empty();
        }

        return Optional.of(avps.get(indexDestHost).ensure().toDestinationHost());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(header.toString());
        sb.append(", AVP Count: ").append(avps.size());
        return sb.toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }

        try {
            return DiameterEquality.equals(this, (DiameterMessage)other);
        } catch (final ClassCastException e) {
            return false;
        }

    }
}
