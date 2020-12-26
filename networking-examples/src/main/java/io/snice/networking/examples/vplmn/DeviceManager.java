package io.snice.networking.examples.vplmn;

import io.hektor.actors.fsm.FsmActor;
import io.hektor.actors.fsm.OnStartFunction;
import io.hektor.core.ActorRef;
import io.hektor.core.Hektor;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;
import io.snice.functional.Either;
import io.snice.networking.examples.gtp.GtpConfig;
import io.snice.networking.examples.vplmn.fsm.DeviceManagerContext;
import io.snice.networking.examples.vplmn.fsm.DeviceManagerData;
import io.snice.networking.examples.vplmn.fsm.DeviceManagerEvent;
import io.snice.networking.examples.vplmn.fsm.DeviceManagerFsm;
import io.snice.networking.examples.vplmn.fsm.device.DeviceConfiguration;
import io.snice.networking.examples.vplmn.fsm.device.DeviceContext;
import io.snice.networking.examples.vplmn.fsm.device.DeviceData;
import io.snice.networking.examples.vplmn.fsm.device.DeviceFsm;
import io.snice.networking.gtp.GtpEnvironment;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static io.snice.preconditions.PreConditions.assertNotEmpty;
import static io.snice.preconditions.PreConditions.assertNotNull;

public interface DeviceManager {

    CompletionStage<Either<Error, Device>> addDevice(final String imei);

    static DeviceManager of(final Hektor hektor, final GtpEnvironment<GtpConfig> environment) {
        assertNotNull(hektor);
        assertNotNull(environment);

        final Function<ActorRef, DeviceManagerContext> ctxFactory = (ref) -> new DefaultDeviceManagerCtx(ref, environment);

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

        private DefaultDeviceManagerCtx(final ActorRef self, final GtpEnvironment<GtpConfig> environment) {
            this.self = self;
            this.environment = environment;
        }

        @Override
        public Device createDevice(final String imei) {
            final Function<ActorRef, DeviceContext> ctxFactory = (ref) -> new DefaultDeviceCtx(ref, imei, environment);

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
        private final ActorRef self;
        private final String imei;
        private final GtpEnvironment<GtpConfig> environment;

        private DefaultDeviceCtx(final ActorRef self, final String imei, final GtpEnvironment<GtpConfig> environment) {
            this.self = self;
            this.imei = imei;
            this.environment = environment;
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
        public void initiatePdnSession(final CreateSessionRequest csr) {
            environment.initiateNewPdnSession(csr)
                    .withRemoteIPv4("127.0.0.1")
                    .start()
                    .thenAccept(self::tell);
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
            me.tell("go_online");
        }

        @Override
        public void goOffline() {

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
