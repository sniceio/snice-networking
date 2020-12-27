package io.snice.networking.examples.vplmn;

import io.hektor.actors.fsm.FsmActor;
import io.hektor.actors.fsm.OnStartFunction;
import io.hektor.core.ActorRef;
import io.hektor.core.Hektor;
import io.snice.buffer.Buffer;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionResponse;
import io.snice.functional.Either;
import io.snice.networking.examples.gtp.GtpConfig;
import io.snice.networking.examples.vplmn.fsm.DeviceManagerContext;
import io.snice.networking.examples.vplmn.fsm.DeviceManagerData;
import io.snice.networking.examples.vplmn.fsm.DeviceManagerEvent;
import io.snice.networking.examples.vplmn.fsm.DeviceManagerFsm;
import io.snice.networking.examples.vplmn.fsm.device.DeviceConfiguration;
import io.snice.networking.examples.vplmn.fsm.device.DeviceContext;
import io.snice.networking.examples.vplmn.fsm.device.DeviceData;
import io.snice.networking.examples.vplmn.fsm.device.DeviceEvent;
import io.snice.networking.examples.vplmn.fsm.device.DeviceFsm;
import io.snice.networking.gtp.Bearer;
import io.snice.networking.gtp.GtpControlTunnel;
import io.snice.networking.gtp.GtpEnvironment;
import io.snice.networking.gtp.PdnSessionContext;
import io.snice.networking.gtp.impl.DefaultEpsBearer;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static io.snice.preconditions.PreConditions.assertNotEmpty;
import static io.snice.preconditions.PreConditions.assertNotNull;

public interface DeviceManager {

    CompletionStage<Either<Error, Device>> addDevice(final String imei);

    static DeviceManager of(final Hektor hektor, final GtpEnvironment<GtpConfig> environment, final GtpControlTunnel tunnel) {
        assertNotNull(hektor);
        assertNotNull(environment);
        assertNotNull(tunnel);

        final Function<ActorRef, DeviceManagerContext> ctxFactory = (ref) -> new DefaultDeviceManagerCtx(ref, environment, tunnel);

        final var props = FsmActor.of(DeviceManagerFsm.definition)
                .withContext(ctxFactory)
                .withData(() -> new DeviceManagerData())
                .build();

        final var actorRef = hektor.actorOf("devices", props);
        final var manager = new DeviceManagerActor(hektor, actorRef);
        actorRef.tell("run");
        return manager;
    }

    class DefaultDeviceManagerCtx implements DeviceManagerContext {

        private final ActorRef self;
        private final GtpEnvironment<GtpConfig> environment;
        private final GtpControlTunnel tunnel;

        private DefaultDeviceManagerCtx(final ActorRef self, final GtpEnvironment<GtpConfig> environment, final GtpControlTunnel tunnel) {
            this.self = self;
            this.environment = environment;
            this.tunnel = tunnel;
        }

        @Override
        public Device createDevice(final String imei) {
            final Function<ActorRef, DeviceContext> ctxFactory = (ref) -> new DefaultDeviceCtx(ref, imei, environment, tunnel);

            final OnStartFunction<DeviceContext, DeviceData> onstart = (actorCtx, ctx, data) -> {
                System.err.println("yeah, we started");
            };

            final var props = FsmActor.of(DeviceFsm.definition)
                    .withContext(ctxFactory)
                    .withData(new DeviceData())
                    .withStartFunction(onstart)
                    .build();

            final var deviceRef = ctx().actorOf(imei, props);
            return new DeviceManagerActor.DefaultDevice(deviceRef, imei);
        }
    }

    class DefaultDeviceCtx implements DeviceContext {
        private final int defaultGtpuPort = 2152;

        private final ActorRef self;
        private final String imei;
        private final GtpEnvironment<GtpConfig> environment;
        private final GtpControlTunnel tunnel;

        private DefaultDeviceCtx(final ActorRef self, final String imei, final GtpEnvironment<GtpConfig> environment, final GtpControlTunnel tunnel) {
            this.self = self;
            this.imei = imei;
            this.environment = environment;
            this.tunnel = tunnel;
        }

        @Override
        public String getImei() {
            return imei;
        }

        @Override
        public DeviceConfiguration getConfiguration() {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public void send(final GtpMessage msg) {
            if (msg.isGtpVersion2() && msg.isRequest()) {
                final var request = msg.toGtp2Request();
                tunnel.createNewTransaction(request)
                        .onAnswer((t, resp) -> self.tell(new DeviceEvent.GtpResponseEvent(t, resp)))
                        .start();
            } else {
                tunnel.send(msg);
            }
        }

        @Override
        public void establishBearer(final Bearer local, final Bearer remote, final Buffer assignedIpAddress, final int localPort) {
            final var remoteIp = remote.getIPv4AddressAsString().get();
            // TODO: if there is a NAT between us and e.g. the PGW, we need to
            // NAT the IP...
            environment.establishUserPlane(remoteIp, defaultGtpuPort).thenAccept(tunnel -> {
                final var epsBearer = DefaultEpsBearer.create(tunnel, assignedIpAddress, local, remote, localPort);
                final var evt = new DeviceEvent.EpsBearerEstablished(epsBearer);
                self.tell(evt);
            }).exceptionally(t -> {
                // TODO: issue a failed eps bearer event so the FSM can handle it.
                return null;
            });
        }

        @Override
        public PdnSessionContext createPdnSessionContext(final CreateSessionRequest req, final CreateSessionResponse resp) {
            // TODO: we will probably do more here...
            return PdnSessionContext.of(req, resp);
        }

    }

    class DefaultDevice implements Device {

        private final ActorRef me;
        private final String imei;

        private DefaultDevice(final ActorRef me, final String imei) {
            this.me = me;
            this.imei = imei;
        }

        @Override
        public String getImei() {
            return imei;
        }

        @Override
        public void goOnline() {
            me.tell(DeviceEvent.PRE_AUTHED);
            me.tell(DeviceEvent.PRE_ATTACHED);
            me.tell(DeviceEvent.INITIATE_SESSION);
        }

        @Override
        public void goOffline() {


        }

        @Override
        public void sendData(final Buffer data, final String remoteIp, final int remotePort) {
            final var dataEvt = new DeviceEvent.SendDataEvent(data, remoteIp, remotePort);
            me.tell(dataEvt);
        }
    }

    class DeviceManagerActor implements DeviceManager {

        private final Hektor hektor;
        private final ActorRef me;

        private DeviceManagerActor(final Hektor hektor, final ActorRef me) {
            this.hektor = hektor;
            this.me = me;
        }

        @Override
        public CompletionStage<Either<Error, Device>> addDevice(final String imei) {
            assertNotEmpty(imei, "The IMEI of the new device cannot be null or the empty String");
            final var evt = DeviceManagerEvent.ManageDeviceEvent.manageDevice(imei);
            return me.ask(evt).thenApply(result -> {
                if (result instanceof Error) {
                    return Either.left((Error) result);
                }
                return Either.right((Device) result);
            });
        }
    }

}
