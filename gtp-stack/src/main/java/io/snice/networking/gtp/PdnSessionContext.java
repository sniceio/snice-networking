package io.snice.networking.gtp;

import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Response;
import io.snice.codecs.codec.gtp.gtpc.v2.Impl.Gtp2MessageBuilder;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Paa;
import io.snice.networking.gtp.impl.DefaultPdnSessionContext;

/**
 * Keeps track of the various parameters that make up a PDN session.
 */
public interface PdnSessionContext {

    static PdnSessionContext of(final CreateSessionRequest csr, final Gtp2Response response) {
        return DefaultPdnSessionContext.of(csr, response);
    }

    Bearer getDefaultLocalBearer();

    Bearer getDefaultRemoteBearer();

    /**
     * Convenience method for getting the {@link Teid} off of the {@link #getDefaultLocalBearer()}
     */
    default Teid getLocalBearerTeid() {
        return getDefaultLocalBearer().getTeid();
    }

    Teid getLocalTeid();

    /**
     * Convenience method for getting the {@link Teid} off of the {@link #getDefaultRemoteBearer()}
     */
    default Teid getRemoteBearerTeid() {
        return getDefaultRemoteBearer().getTeid();
    }

    Teid getRemoteTeid();

    /**
     * Get the PAA (PDN Address Allocation). I.e., the IP address that got
     * assigned to the device by the PGW.
     */
    Paa getPaa();

    /**
     * The Create Session request that initiated this
     * PDN session, which may, or may not, have been successful
     * at the end.
     */
    CreateSessionRequest getCreateSessionRequest();

    /**
     * Convenience method for creating a Delete Session Request based on this context, which means we will pull out
     * the correct remote TEID, set the EBI etc.
     *
     * @return a delete session request that can be used to delete this pdn session.
     */
    Gtp2MessageBuilder<Gtp2Message> createDeleteSessionRequest();

    /**
     * The Create Session Response, which either established the
     * pdn session or indicated that it failed for some reason.
     */
    Gtp2Response getCreateSessionResponse();
}
