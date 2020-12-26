package io.snice.networking.examples.vplmn.fsm.device;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.MccMnc;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Uli;
import io.snice.codecs.codec.gtp.gtpc.v2.type.EcgiField;
import io.snice.codecs.codec.gtp.gtpc.v2.type.RatType;
import io.snice.codecs.codec.gtp.gtpc.v2.type.TaiField;
import io.snice.codecs.codec.gtp.gtpc.v2.type.UliType;
import io.snice.networking.gtp.PdnSession;

import static io.snice.codecs.codec.gtp.gtpc.v2.type.PdnType.Type.IPv4;
import static io.snice.networking.examples.vplmn.fsm.device.DeviceState.DEAD;
import static io.snice.networking.examples.vplmn.fsm.device.DeviceState.INITIATE_PDN_SESSION;
import static io.snice.networking.examples.vplmn.fsm.device.DeviceState.OFFLINE;
import static io.snice.networking.examples.vplmn.fsm.device.DeviceState.ONLINE;

public class DeviceFsm {

    public static final Definition<DeviceState, DeviceContext, DeviceData> definition;

    static {
        final var builder = FSM.of(DeviceState.class).ofContextType(DeviceContext.class).withDataType(DeviceData.class);
        final var offline = builder.withInitialState(OFFLINE);
        final var pdnSession = builder.withState(INITIATE_PDN_SESSION);
        final var online = builder.withState(ONLINE);
        final var dead = builder.withFinalState(DEAD);

        offline.transitionTo(INITIATE_PDN_SESSION)
                .onEvent(String.class)
                .withGuard("go_online"::equals)
                .withAction(DeviceFsm::initiatePdnSession);

        pdnSession.transitionTo(ONLINE).onEvent(PdnSession.class).withAction(DeviceFsm::processPdnSession);
        online.transitionTo(DEAD).onEvent(String.class).withGuard("die"::equals);

        definition = builder.build();
    }

    private static void initiatePdnSession(final String evt, final DeviceContext ctx, final DeviceData data) {
        final var csr = createCSR();
        ctx.initiatePdnSession(csr);
    }

    private static void processPdnSession(final PdnSession session, final DeviceContext ctx, final DeviceData data) {
        System.err.println("Processing PdnSesison: " + session);
        final var self = ctx.self();
        session.establishDefaultBearer().thenAccept(self::tell);
    }

    private static CreateSessionRequest createCSR() {

        // If the PGW is behind a NAT, make sure you grab the public address (duh)
        final var pgw = "127.0.0.1";

        // If you're behind a NAT, you want the NAT:ed address here. Otherwise, your
        // local NIC is fine. All depends where the PGW is...
        final var sgw = "127.0.0.1";
        final var imsi = "999994000000642";

        final var csr = CreateSessionRequest.create()
                .withTeid(Teid.ZEROS)
                .withRat(RatType.EUTRAN)
                .withAggregateMaximumBitRate(10000, 10000)
                .withImsi(imsi)
                .withServingNetwork("310/410")
                .withTliv(createUli())
                .withApnSelectionMode(0)
                .withApn("super")
                .withNoApnRestrictions()
                .withPdnType(IPv4)
                .withIPv4PdnAddressAllocation("0.0.0.0")
                .withNewSenderControlPlaneFTeid()
                .withRandomizedTeid()
                .withIPv4Address(sgw)
                .doneFTeid()
                .withNewBearerContext()
                .withNewSgwFTeid()
                .withRandomizedTeid()
                .withIPv4Address(sgw)
                .doneFTeid()
                .withEpsBearerId(5)
                .withNewBearerQualityOfService(9)
                .withPriorityLevel(10)
                .withPci()
                .doneBearerQoS()
                .doneBearerContext()
                .build();
        return csr;
    }

    private static Uli createUli() {
        final var tac = Buffers.wrap((byte) 0x02, (byte) 0x01);
        final var tai = TaiField.of(MccMnc.of("901", "62"), tac);
        final var eci = Buffers.wrap((byte) 0x00, (byte) 0x11, (byte) 0xAA, (byte) 0xBB);
        final var ecgi = EcgiField.of(MccMnc.of("901", "62"), eci);
        return Uli.ofValue(UliType.create().withTai(tai).withEcgi(ecgi).build());
    }
}
