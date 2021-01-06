package io.snice.networking.gtp.impl;

import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2MessageType;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Response;
import io.snice.codecs.codec.gtp.gtpc.v2.Impl.Gtp2MessageBuilder;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.BearerContext;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.FTeid;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Paa;
import io.snice.networking.gtp.Bearer;
import io.snice.networking.gtp.PdnSessionContext;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertArgument;

public class DefaultPdnSessionContext implements PdnSessionContext {

    private final CreateSessionRequest request;
    private final Gtp2Response response;

    private final Teid localTeid;
    private final Teid remoteTeid;

    private final FTeid remoteFTeid;

    private final Paa paa;
    private final Bearer defaultLocalBearer;
    private final Bearer defaultRemoteBearer;

    public static PdnSessionContext of(final CreateSessionRequest csr, final Gtp2Response response) {
        assertArgument(response.isCreateSessionResponse(), "The response must be a Create Session Response");
        assertArgument(csr.getHeader().getSequenceNo().equals(response.getHeader().getSequenceNo()),
                "The request/response has difference sequence numbers. They do not belong to the same transaction.");

        // TODO: check cause. For now, assume happy case...

        final var paa = response.getInfoElement(Paa.TYPE, 0);
        assertArgument(paa.isPresent(), "No PAA in the Create Session Request");

        // our local TEID will be in the header
        final var localTeid = response.getHeader().toGtp2Header().getTeid().get();
        final var localBearerContext = getBearerContext(csr, 0);
        assertArgument(localBearerContext.isPresent(), "No Bearer Context in the Create Session Request");

        // the remote TEID will be in the FTEID
        final var remoteBearerContext = getBearerContext(response, 0);
        final var fteidGtpc = getFTeid(response, 1);

        assertArgument(remoteBearerContext.isPresent(), "No Bearer Context in the Create Session Response");
        assertArgument(fteidGtpc.isPresent(), "No GTP-C FTEID found in the Create Session Response");

        final var defaultLocalBearer = Bearer.of(localBearerContext.get());
        final var defaultRemoteBearer = Bearer.of(remoteBearerContext.get());

        return new DefaultPdnSessionContext(csr, response, localTeid, fteidGtpc.get(), (Paa) paa.get().ensure(), defaultLocalBearer, defaultRemoteBearer);
    }

    private DefaultPdnSessionContext(final CreateSessionRequest request, final Gtp2Response response,
                                     final Teid localTeid, final FTeid remoteFTeid,
                                     final Paa paa, final Bearer localBearer, final Bearer remoteBearer) {
        this.request = request;
        this.response = response;
        this.localTeid = localTeid;
        this.remoteTeid = remoteFTeid.getValue().getTeid();
        this.remoteFTeid = remoteFTeid;
        this.paa = paa;
        this.defaultLocalBearer = localBearer;
        this.defaultRemoteBearer = remoteBearer;
    }

    @Override
    public Gtp2MessageBuilder<Gtp2Message> createDeleteSessionRequest() {
        final var ebiMaybe = defaultRemoteBearer.getEbi();

        final var builder = Gtp2Message.create(Gtp2MessageType.DELETE_SESSION_REQUEST)
                .withTeid(remoteTeid)
                .withRandomSeqNo();
        ebiMaybe.ifPresent(builder::withTliv);

        return builder;
    }

    @Override
    public CreateSessionRequest getCreateSessionRequest() {
        return request;
    }

    @Override
    public Gtp2Response getCreateSessionResponse() {
        return response;
    }

    @Override
    public Bearer getDefaultLocalBearer() {
        return defaultLocalBearer;
    }

    @Override
    public Bearer getDefaultRemoteBearer() {
        return defaultRemoteBearer;
    }

    @Override
    public Teid getLocalTeid() {
        return localTeid;
    }

    @Override
    public Teid getRemoteTeid() {
        return remoteTeid;
    }

    @Override
    public Paa getPaa() {
        return paa;
    }

    private static Optional<BearerContext> getBearerContext(final Gtp2Message msg, final int instance) {
        return msg.getInfoElement(BearerContext.TYPE, instance).map(tliv -> (BearerContext) (tliv.ensure()));
    }

    private static Optional<FTeid> getFTeid(final Gtp2Message msg, final int instance) {
        return msg.getInfoElement(FTeid.TYPE).map(tliv -> (FTeid) (tliv.ensure()));
    }
}
