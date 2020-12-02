package io.snice.networking.gtp;

import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.BearerContext;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Ebi;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.FTeid;
import io.snice.networking.gtp.impl.DefaultBearer;

import java.util.Optional;

public interface Bearer {

    static Bearer of(final BearerContext ctx) {
        return DefaultBearer.of(ctx);
    }

    /**
     * Get the optional {@link Ebi}.
     */
    Optional<Ebi> getEbi();

    /**
     * Get the mandatory {@link FTeid}.
     */
    FTeid getFTeid();

    /**
     * Get the optional IPv4 address. A given {@link FTeid} will
     * either have an ipv4 or an ipv6 address but one will be present.
     * <p>
     * This is just a convenience method for <code> getFTeid().getValue().getIpv4Address(); </code>
     */
    default Optional<Buffer> getIPv4Address() {
        return getFTeid().getValue().getIpv4Address();
    }

    default Optional<String> getIPv4AddressAsString() {
        return getFTeid().getValue().getIpv4AddressAsString();
    }

    /**
     * Get the {@link Teid}
     * <p>
     * This is just a convenience method for <code> getFTeid().getValue().getTeid(); </code>
     */
    default Teid getTeid() {
        return getFTeid().getValue().getTeid();
    }
}
