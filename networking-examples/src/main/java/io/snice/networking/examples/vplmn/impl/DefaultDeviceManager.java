package io.snice.networking.examples.vplmn.impl;

import io.hektor.actors.events.subscription.SubscribeEvent;
import io.hektor.actors.fsm.FsmActor;
import io.hektor.actors.fsm.OnStartFunction;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Hektor;
import io.snice.buffer.Buffer;
import io.snice.codecs.codec.Imei;
import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionResponse;
import io.snice.functional.Either;
import io.snice.networking.examples.gtp.GtpConfig;
import io.snice.networking.examples.vplmn.Device;
import io.snice.networking.examples.vplmn.DeviceManager;
import io.snice.networking.examples.vplmn.Error;
import io.snice.networking.examples.vplmn.SimCard;
import io.snice.networking.examples.vplmn.fsm.devices.device.DeviceConfiguration;
import io.snice.networking.examples.vplmn.fsm.devices.device.DeviceContext;
import io.snice.networking.examples.vplmn.fsm.devices.device.DeviceData;
import io.snice.networking.examples.vplmn.fsm.devices.device.DeviceEvent;
import io.snice.networking.examples.vplmn.fsm.devices.device.DeviceFsm;
import io.snice.networking.gtp.Bearer;
import io.snice.networking.gtp.GtpControlTunnel;
import io.snice.networking.gtp.GtpEnvironment;
import io.snice.networking.gtp.PdnSessionContext;
import io.snice.networking.gtp.impl.DefaultEpsBearer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultDeviceManager implements InternalDeviceManager {

    private final Hektor hektor;
    private final ActorPath rootDevicePath;
    private final GtpEnvironment<GtpConfig> environment;
    private final GtpControlTunnel tunnel;

    private final ConcurrentMap<Imei, DefaultDevice> devices = new ConcurrentHashMap<>();

    public static DeviceManager of(final Hektor hektor, final GtpEnvironment<GtpConfig> environment, final GtpControlTunnel tunnel) {
        assertNotNull(hektor);
        assertNotNull(environment);
        assertNotNull(tunnel);

        /*
        final Function<ActorRef, DeviceManagerContext> ctxFactory = (ref) -> new DefaultDeviceManagerCtx(ref, environment, tunnel);

        final var props = FsmActor.of(DeviceManagerFsm.definition)
                .withContext(ctxFactory)
                .withData(() -> new DeviceManagerData())
                .build();

        final var actorRef = hektor.actorOf("devices", props);
        actorRef.tell("run");
         */
        return new DefaultDeviceManager(hektor, environment, tunnel);
    }

    private DefaultDeviceManager(final Hektor hektor, final GtpEnvironment<GtpConfig> environment, final GtpControlTunnel tunnel) {
        this.hektor = hektor;
        rootDevicePath = ActorPath.of("devices");
        this.environment = environment;
        this.tunnel = tunnel;
    }

    @Override
    public CompletionStage<Either<Error, Device>> createDevice(final Device.Type type, final SimCard sim) {
        assertNotNull(type);
        assertNotNull(sim);
        final var imei = Imei.random();
        // final var evt = new DeviceManagerEvent.CreateDeviceRequest(type, sim);
        // self.tell(evt);
        // assertNotEmpty(imei, "The IMEI of the new device cannot be null or the empty String");
        // final var evt = DeviceManagerEvent.CreateDeviceRequest.createDeviceRequest(null);
            /*
            return me.ask(evt).thenApply(result -> {
                if (result instanceof Error) {
                    return Either.left((Error) result);
                }
                return Either.right((Device) result);
            });
             */
        final Function<ActorRef, DeviceContext> ctxFactory = (ref) -> new DefaultDeviceCtx(ref, imei, sim, environment, tunnel);

        final OnStartFunction<DeviceContext, DeviceData> onstart = (actorCtx, ctx, data) -> {
            System.err.println("yeah, we started");
        };

        final var props = FsmActor.of(DeviceFsm.definition)
                .withContext(ctxFactory)
                .withData(new DeviceData())
                .withStartFunction(onstart)
                .build();

        final var deviceRef = hektor.actorOf(rootDevicePath, imei.toString(), props);
        final var device = new DefaultDevice(deviceRef, imei, sim);
        devices.put(imei, device);
        return CompletableFuture.completedFuture(Either.right(device));
    }

    @Override
    public void claim(final Imei imei, final ActorRef owner) {
        final var device = devices.get(imei);
        assertNotNull(device, "No such device");
        device.self.tell(SubscribeEvent.subscriber(owner));
    }

    private static class DefaultDeviceCtx implements DeviceContext {
        private final int defaultGtpuPort = 2152;

        private final ActorRef self;
        private final Imei imei;
        private final SimCard simCard;
        private final GtpEnvironment<GtpConfig> environment;
        private final GtpControlTunnel tunnel;

        private DefaultDeviceCtx(final ActorRef self,
                                 final Imei imei,
                                 final SimCard simCard,
                                 final GtpEnvironment<GtpConfig> environment,
                                 final GtpControlTunnel tunnel) {
            this.self = self;
            this.imei = imei;
            this.simCard = simCard;
            this.environment = environment;
            this.tunnel = tunnel;
        }

        @Override
        public Imei getImei() {
            return imei;
        }

        @Override
        public SimCard getSimCard() {
            return simCard;
        }

        @Override
        public DeviceConfiguration getConfiguration() {
            throw new RuntimeException("Not yet implemented");
        }

        @Override
        public void deviceIsOnline() {
            tellSubscribers(DeviceEvent.Online.fullService(imei));
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

    private static class DefaultDevice implements Device {

        private final ActorRef self;
        private final Imei imei;
        private final SimCard simCard;

        private DefaultDevice(final ActorRef self, final Imei imei, final SimCard simCard) {
            this.self = self;
            this.imei = imei;
            this.simCard = simCard;
        }

        @Override
        public Imei getImei() {
            return imei;
        }

        @Override
        public SimCard getSimCard() {
            return simCard;
        }

        @Override
        public void goOnline() {
            self.tell(DeviceEvent.PRE_AUTHED);
            self.tell(DeviceEvent.PRE_ATTACHED);
            self.tell(DeviceEvent.INITIATE_SESSION);
        }

        @Override
        public void goOffline() {


        }

        @Override
        public void sendData(final Buffer data, final String remoteIp, final int remotePort) {
            final var dataEvt = new DeviceEvent.SendDataEvent(data, remoteIp, remotePort);
            self.tell(dataEvt);
        }
    }

}
