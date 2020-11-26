package io.snice.networking.gtp.impl;

import io.snice.buffer.Buffers;
import io.snice.codecs.codec.MccMnc;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2MessageType;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Request;
import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Response;
import io.snice.codecs.codec.gtp.gtpc.v2.Impl.Gtp2MessageBuilder;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.*;
import io.snice.codecs.codec.gtp.gtpc.v2.type.*;
import io.snice.codecs.codec.tgpp.ReferencePoint;
import io.snice.functional.Either;
import io.snice.networking.gtp.Bearer;
import io.snice.networking.gtp.GtpTunnel;
import io.snice.networking.gtp.PdnSession;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static io.snice.preconditions.PreConditions.*;

public class DefaultPdnSession implements PdnSession {

    private final Gtp2Request request;

    private final Gtp2Response response;

    private final Teid localTeid;
    private final Teid remoteTeid;

    private final FTeid remoteFTeid;

    private final Paa paa;
    private final Bearer defaultLocalBearer;
    private final Bearer defaultRemoteBearer;

    public static PdnSession of(final Gtp2Request request, final Gtp2Response response) {
        assertArgument(request.isCreateSessionRequest(), "The request must be a Create Session Request");
        assertArgument(response.isCreateSessionResponse(), "The response must be a Create Session Response");
        assertArgument(request.getHeader().getSequenceNo().equals(response.getHeader().getSequenceNo()),
                "The request/response has difference sequence numbers. They do not belong to the same transaction.");

        // TODO: check cause. For now, assume happy case...

        final var paa = response.getInformationElement(Paa.TYPE, 0);
        assertArgument(paa.isPresent(), "No PAA in the Create Session Request");

        // our local TEID will be in the header
        final var localTeid = response.getHeader().toGtp2Header().getTeid().get();
        final var localBearerContext = getBearerContext(request, 0);
        assertArgument(localBearerContext.isPresent(), "No Bearer Context in the Create Session Request");

        // the remote TEID will be in the FTEID
        final var remoteBearerContext = getBearerContext(response, 0);
        final var fteidGtpc = getFTeid(response, 1);

        assertArgument(remoteBearerContext.isPresent(), "No Bearer Context in the Create Session Response");
        assertArgument(fteidGtpc.isPresent(), "No GTP-C FTEID found in the Create Session Response");

        final var defaultLocalBearer = Bearer.of(localBearerContext.get());
        final var defaultRemoteBearer = Bearer.of(remoteBearerContext.get());

        return new DefaultPdnSession(request, response, localTeid, fteidGtpc.get(), (Paa) paa.get().ensure(), defaultLocalBearer, defaultRemoteBearer);
    }

    public static Builder createNewSession(final GtpTunnel tunnel, final String imsi) {
        assertNotEmpty(imsi, "The IMSI cannot be null or the empty string");
        return new DefaultBuilder(tunnel, Imsi.ofValue(imsi));
    }

    public static Builder createNewSession(final GtpTunnel tunnel, final Imsi imsi) {
        assertNotNull(imsi, "The IMSI cannot be null");
        return new DefaultBuilder(tunnel, imsi);
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

    private DefaultPdnSession(final Gtp2Request request, final Gtp2Response response,
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
    public Gtp2Request getCreateSessionRequest() {
        return request;
    }

    @Override
    public Paa getPaa() {
        return paa;
    }

    @Override
    public Gtp2Response getCreateSessionResponse() {
        return response;
    }

    @Override
    public Gtp2Request createDeleteSessionRequest() {
        final var ebiMaybe = defaultRemoteBearer.getEbi();

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

    private static class DefaultBuilder implements Builder {
        private final Gtp2MessageBuilder<Gtp2Message> csr;
        private final GtpTunnel tunnel;

        private DefaultBuilder(final GtpTunnel tunnel, final Imsi imsi) {
            csr = Gtp2Message.create(Gtp2MessageType.CREATE_SESSION_REQUEST)
                    .withTeid(Teid.ZEROS) // for initial CSR, the TEID must be zero
                    .withRandomSeqNo()
                    .withTliv(imsi);
            this.tunnel = tunnel;
        }

        @Override
        public Builder withServingNetwork(final String mccMnc) {
            csr.withTliv(ServingNetwork.ofValue(MccMncType.ofValue(mccMnc)));
            return this;
        }

        @Override
        public Builder withRat(final int rat) {
            csr.withTliv(Rat.ofValue(RatType.of(rat)));
            return this;
        }

        @Override
        public Builder withRat(final RatType rat) {
            csr.withTliv(Rat.ofValue(rat));
            return this;
        }

        @Override
        public Builder withApn(final String apn) {
            csr.withTliv(Apn.ofValue(apn));
            return this;
        }

        @Override
        public Builder withUeTimeZone(final UeTimeZone tz) {
            // TODO: make more user friendly version of this
            // UeTimeZone.ofValue(Buffers.wrap((byte) 0x08, (byte) 0x00));
            assertNotNull(tz, "The UE Time Zone cannot be null");
            csr.withTliv(tz);
            return this;
        }

        @Override
        public Builder withAggregateMaximumBitRate(final int maxUplink, final int maxDownlink) {
            csr.withTliv(Ambr.ofValue(AmbrType.ofValue(maxUplink, maxDownlink)));
            return this;
        }

        @Override
        public CompletionStage<Either<String, PdnSession>> start() {
            final var message = csr.build();
            tunnel.send(message);
            return new CompletableFuture<>();
        }

        private static Uli createUli() {
            final var tac = Buffers.wrap((byte) 0x02, (byte) 0x01);
            final var tai = TaiField.of(MccMnc.of("901", "62"), tac);
            final var eci = Buffers.wrap((byte) 0x00, (byte) 0x11, (byte) 0xAA, (byte) 0xBB);
            final var ecgi = EcgiField.of(MccMnc.of("901", "62"), eci);
            return Uli.ofValue(UliType.create().withTai(tai).withEcgi(ecgi).build());
        }

        private static FTeid createFTeidGtpc(final String ipv4Address) {
            final var ftiedTypeGtpC = FTeidType.create()
                    .withRandomizedTeid()
                    .withIPv4Address(ipv4Address)
                    .withReferencePoint(ReferencePoint.S5, true) // true = gtp-c
                    .build();

            return FTeid.ofValue(ftiedTypeGtpC, 0);
        }

        private Gtp2Request createCsr(final String gtpcTunnelIPv4) {
            final var arp = ArpType.ofValue(10, true, false);
            final var qos = QosType.ofQci(9).build();
            final var bqos = BearerQos.ofValue(BearerQosType.ofValue(arp, qos));

            final var ftiedTypeGtpU = FTeidType.create()
                    .withRandomizedTeid()
                    .withIPv4Address(gtpcTunnelIPv4)
                    .withReferencePoint(ReferencePoint.S5, false) // false = gtp-u
                    .build();

            final var fteidGtpC = createFTeidGtpc(gtpcTunnelIPv4);
            final var fteidGtpU = FTeid.ofValue(ftiedTypeGtpU, 2);

            final var ebi = Ebi.ofValue(EbiType.ofValue(5));
            final var grouped = GroupedType.ofValue(ebi, fteidGtpU, bqos);

            final var uli = createUli();

            final var bearerContext = BearerContext.ofValue(grouped);

            final var csr = Gtp2Message.create(Gtp2MessageType.CREATE_SESSION_REQUEST)
                    .withTliv(Mei.ofValue(TbcdType.parse("1234567890123456")))
                    .withTliv(fteidGtpC)
                    .withTliv(SelectionMode.ofValue(SelectionModeType.ofValue(0)))
                    .withTliv(Pdn.ofValue(PdnType.of(PdnType.Type.IPv4)))
                    .withTliv(Paa.ofValue(PaaType.fromIPv4("0.0.0.0")))
                    .withTliv(ApnRestriction.ofValue(CounterType.parse("0")))
                    .withTliv(bearerContext)
                    .withTliv(uli)
                    .build();

            return csr.toGtp2Request();
        }

    }


}
