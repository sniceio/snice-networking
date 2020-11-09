package io.snice.networking.gtp.impl;

import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2MessageType;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Response;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.BearerContext;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Ebi;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.FTeid;
import io.snice.networking.gtp.PdnSession;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertArgument;

public class DefaultPdnSession implements PdnSession {

    private final Gtp2Request request;

    private final Gtp2Response response;

    private final Teid localTeid;
    private final Teid remoteTeid;

    private final FTeid remoteFTeid;

    private final BearerContext bearerContext;

    public static PdnSession of(final Gtp2Request request, final Gtp2Response response) {
        assertArgument(request.isCreateSessionRequest(), "The request must be a Create Session Request");
        assertArgument(response.isCreateSessionResponse(), "The response must be a Create Session Response");
        assertArgument(request.getHeader().getSequenceNo().equals(response.getHeader().getSequenceNo()),
                "The request/response has difference sequence numbers. They do not belong to the same transaction.");

        // TODO: check cause. For now, assume happy case...

        // our local TEID will be in the header
        final var localTeid = response.getHeader().toGtp2Header().getTeid().get();

        // the remote TEID will be in the FTEID
        final var bearerContext = getBearerContext(response, 0);
        final var fteidGtpc = getFTeid(response, 1);

        assertArgument(bearerContext.isPresent(), "No Bearer Context in the Create Session Response");
        assertArgument(fteidGtpc.isPresent(), "No GTP-C FTEID found in the Create Session Response");

        return new DefaultPdnSession(request, response, localTeid, fteidGtpc.get(), bearerContext.get());
    }

    @Override
    public Teid getLocalTeid() {
        return localTeid;
    }

    private DefaultPdnSession(final Gtp2Request request, final Gtp2Response response,
                              final Teid localTeid, final FTeid remoteFTeid, final BearerContext bearerContext) {
        this.request = request;
        this.response = response;
        this.localTeid = localTeid;
        this.remoteTeid = remoteFTeid.getValue().getTeid();
        this.remoteFTeid = remoteFTeid;
        this.bearerContext = bearerContext;
    }

    @Override
    public Gtp2Request getCreateSessionRequest() {
        return request;
    }

    @Override
    public Gtp2Response getCreateSessionResponse() {
        return response;
    }

    @Override
    public Gtp2Request createDeleteSessionRequest() {
        final var ebiMaybe = bearerContext.getValue().getInformationElement(Ebi.TYPE, 0);

        final var builder = Gtp2Message.create(Gtp2MessageType.DELETE_SESSION_REQUEST).withTeid(remoteTeid);
        ebiMaybe.ifPresent(builder::withTliv);

        return builder.build().toGtp2Request();
    }

    private static Optional<BearerContext> getBearerContext(final Gtp2Message msg, final int instance) {
        return msg.getInformationElement(BearerContext.TYPE, instance).map(tliv -> (BearerContext) (tliv.ensure()));
    }

    private static Optional<FTeid> getFTeid(final Gtp2Message msg, final int instance) {
        return msg.getInformationElement(FTeid.TYPE).map(tliv -> (FTeid) (tliv.ensure()));
    }


}
