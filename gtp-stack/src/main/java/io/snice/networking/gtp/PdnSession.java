package io.snice.networking.gtp;

import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Response;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.*;
import io.snice.codecs.codec.gtp.gtpc.v2.type.AmbrType;
import io.snice.codecs.codec.gtp.gtpc.v2.type.MccMncType;
import io.snice.codecs.codec.gtp.gtpc.v2.type.RatType;
import io.snice.functional.Either;
import io.snice.networking.gtp.impl.DefaultPdnSession;

import java.util.concurrent.CompletionStage;

import static io.snice.preconditions.PreConditions.assertNotNull;

public interface PdnSession {

    // static PdnSession of(final Gtp2Request request, final Gtp2Response response) {
        // return DefaultPdnSession.of(request, response);
    // }

    Bearer getDefaultLocalBearer();

    Bearer getDefaultRemoteBearer();

    /**
     * Convenience method for getting the {@link Teid} off of the {@link #getDefaultLocalBearer()}
     */
    default Teid getLocalTeid() {
        return getDefaultLocalBearer().getTeid();
    }

    /**
     * Convenience method for getting the {@link Teid} off of the {@link #getDefaultRemoteBearer()}
     */
    default Teid getRemoteTeid() {
        return getDefaultRemoteBearer().getTeid();
    }

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
    Gtp2Request getCreateSessionRequest();

    /**
     * The Create Session Response, which either established the
     * pdn session or indicated that it failed for some reason.
     */
    Gtp2Response getCreateSessionResponse();

    Gtp2Request createDeleteSessionRequest();

    interface Builder {

        Builder withServingNetwork(String mccMnc);
        Builder withRat(int rat);
        Builder withRat(RatType rat);
        Builder withApn(String apn);
        Builder withUeTimeZone(UeTimeZone tz);
        Builder withAggregateMaximumBitRate(int maxUplink, int maxDownlink);

        /**
         * Kick-off the {@link PdnSession} and if successful, as in we receive
         * a successful Create Session Response, this {@link CompletionStage}
         * will complete successfully with a {@link PdnSession}
         *
         * @return
         */
        CompletionStage<Either<String, PdnSession>> start();
    }
}
