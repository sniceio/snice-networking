package io.snice.networking.examples.vplmn.fsm.devices.device;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;
import io.snice.buffer.Buffers;
import io.snice.codecs.codec.MccMnc;
import io.snice.codecs.codec.gtp.Teid;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionResponse;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Uli;
import io.snice.codecs.codec.gtp.gtpc.v2.type.EcgiField;
import io.snice.codecs.codec.gtp.gtpc.v2.type.RatType;
import io.snice.codecs.codec.gtp.gtpc.v2.type.TaiField;
import io.snice.codecs.codec.gtp.gtpc.v2.type.UliType;
import io.snice.networking.gtp.EpsBearer;
import io.snice.networking.gtp.PdnSessionContext;

import static io.snice.codecs.codec.gtp.gtpc.v2.type.PdnType.Type.IPv4;
import static io.snice.networking.examples.vplmn.fsm.devices.device.DeviceState.ATTACHED;
import static io.snice.networking.examples.vplmn.fsm.devices.device.DeviceState.AUTHENTICATED;
import static io.snice.networking.examples.vplmn.fsm.devices.device.DeviceState.DEAD;
import static io.snice.networking.examples.vplmn.fsm.devices.device.DeviceState.ESTABLISHING_BEARER;
import static io.snice.networking.examples.vplmn.fsm.devices.device.DeviceState.INITIATE_SESSION;
import static io.snice.networking.examples.vplmn.fsm.devices.device.DeviceState.OFFLINE;
import static io.snice.networking.examples.vplmn.fsm.devices.device.DeviceState.ONLINE;
import static io.snice.networking.examples.vplmn.fsm.devices.device.DeviceState.SESSION_ESTABLISHED;

public class DeviceFsm {

    public static final Definition<DeviceState, DeviceContext, DeviceData> definition;

    static {
        final var builder = FSM.of(DeviceState.class).ofContextType(DeviceContext.class).withDataType(DeviceData.class);
        final var offline = builder.withInitialState(OFFLINE);
        final var initiateSession = builder.withState(INITIATE_SESSION);

        /**
         * We are waiting for a {@link EpsBearer} to be either successfully established
         * of failed. Whichever state decide to transition over to this state is also
         * responsible for actually attempting to establish a bearer since in this state,
         * we are just waiting for a {@link DeviceEvent.EpsBearerEstablished} or a failed
         * version of it (or a timeout).
         */
        final var establishingBearer = builder.withState(ESTABLISHING_BEARER);

        final var sessionEstablished = builder.withTransientState(SESSION_ESTABLISHED);
        final var authenticated = builder.withState(AUTHENTICATED);
        // final var attaching = builder.withState(ATTACHING);
        final var attached = builder.withState(ATTACHED);
        final var online = builder.withState(ONLINE);
        final var dead = builder.withFinalState(DEAD);

        /**
         * The device has been pre-authenticated so we are skipping the AIR/AIA exchange and
         * going straight to authenticated.
         */
        offline.transitionTo(AUTHENTICATED).onEvent(DeviceEvent.class).withGuard(evt -> evt == DeviceEvent.PRE_AUTHED);

        /**
         * The device has been "pre-attached" so we are skipping the ULR/ULA exchange and
         * going straight to attached.
         */
        authenticated.transitionTo(ATTACHED).onEvent(DeviceEvent.class).withGuard(evt -> evt == DeviceEvent.PRE_ATTACHED);

        attached.transitionTo(INITIATE_SESSION)
                .onEvent(DeviceEvent.class)
                .withGuard(evt -> evt == DeviceEvent.INITIATE_SESSION)
                .withAction(DeviceFsm::initiatePdnSession);


        /**
         * By default, as soon as we get a successful Create Session Response
         * we will immediately try and establish the default bearer, which is why
         * we are transitioning to the {@link ESTABLISHING_BEARER} state from here.
         *
         * TODO: we cannot assume it is success so we need a guard on this one... for now, assume happy case.
         */
        initiateSession.transitionTo(ESTABLISHING_BEARER)
                .onEvent(DeviceEvent.GtpResponseEvent.class)
                .withAction(DeviceFsm::processCreateSessionResponse);

        /**
         * If we successfully manage to establish a bearer, then process that
         * event and then transition to {@link SESSION_ESTABLISHED}, which is
         * a transient state that will automatically move the FSM over to
         * {@link ONLINE}.
         */
        establishingBearer.transitionTo(SESSION_ESTABLISHED)
                .onEvent(DeviceEvent.EpsBearerEstablished.class)
                .withAction((evt, ctx, data) -> data.storeEpsBearer(evt.bearer));


        /**
         * The default transition of this transient state. Hektor.io forces us to have a default
         * transition since we marked this state as a transitional state.
         */
        sessionEstablished.transitionTo(ONLINE).asDefaultTransition();

        /**
         * If we are asked to send some data then just find an appropriate
         * bearer and use it to send the data to the remote destination.
         * If we do not have a bearer established, then we'll silently
         * ignore the request to send the data (perhaps we shouldn't?)
         */
        online.transitionTo(ONLINE)
                .onEvent(DeviceEvent.SendDataEvent.class)
                .withAction((evt, ctx, data) -> data.getDefaultBearer().ifPresent(bearer -> bearer.send(evt.remoteIp, evt.remotePort, evt.data)));

        /**
         * We need a terminal state, which isn't really something a "real" device would have, unless
         * you smash your e.g. phone and as such, it is certainly dead. But, since Hektor.io
         * makes us define a terminal state, this is the one and we can get here by sending "die"
         * to the "FSM".
         */
        online.transitionTo(DEAD).onEvent(String.class).withGuard("die"::equals);

        definition = builder.build();
    }

    private static void initiatePdnSession(final DeviceEvent evt, final DeviceContext ctx, final DeviceData data) {
        ctx.send(createCSR());
    }

    /**
     * When we receive a successful response to our CSR, we will try and create a {@link PdnSessionContext} and save
     * that as well as asking to create the default bearer...
     */
    private static void processCreateSessionResponse(final DeviceEvent.GtpResponseEvent evt, final DeviceContext ctx, final DeviceData data) {
        final var request = evt.transaction.getRequest().toCreateSessionRequest();
        final var response = (CreateSessionResponse) evt.response;
        final var session = ctx.createPdnSessionContext(request, response);
        data.storePdnSession(session);

        final var localBearer = session.getDefaultLocalBearer();
        final var remoteBearer = session.getDefaultRemoteBearer();
        final var assignedIpAddress = session.getPaa().getValue().getIPv4Address().get();
        final var localPort = 3455; // TODO: get this from somewhere.
        ctx.establishBearer(localBearer, remoteBearer, assignedIpAddress, localPort);
    }

    private static CreateSessionRequest createCSR() {

        // If the PGW is behind a NAT, make sure you grab the public address (duh)
        final var pgw = "127.0.0.1";

        // If you're behind a NAT, you want the NAT:ed address here. Otherwise, your
        // local NIC is fine. All depends where the PGW is...
        final var sgw = "127.0.0.1";
        final var imsi = "999994000000642";

        final var csr = CreateSessionRequest.create()
                .withRandomSeqNo() // IMPORTANT!
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
