package io.snice.networking.examples.vplmn.impl;

import io.hektor.actors.fsm.FsmActor;
import io.hektor.actors.fsm.OnStartFunction;
import io.hektor.core.ActorPath;
import io.hektor.core.ActorRef;
import io.hektor.core.Hektor;
import io.snice.functional.Either;
import io.snice.networking.examples.vplmn.Device;
import io.snice.networking.examples.vplmn.Error;
import io.snice.networking.examples.vplmn.SimCard;
import io.snice.networking.examples.vplmn.SimCardManager;
import io.snice.networking.examples.vplmn.User;
import io.snice.networking.examples.vplmn.UserManager;
import io.snice.networking.examples.vplmn.fsm.users.user.AliceFsm;
import io.snice.networking.examples.vplmn.fsm.users.user.UserContext;
import io.snice.networking.examples.vplmn.fsm.users.user.UserData;
import io.snice.networking.examples.vplmn.fsm.users.user.UserEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultUserManager implements UserManager {

    private final Hektor hektor;
    private final InternalDeviceManager deviceManager;
    private final SimCardManager simCardManager;
    private final ActorPath rootUserPath;

    public static UserManager of(final Hektor hektor, final InternalDeviceManager deviceManager, final SimCardManager simCardManager) {
        assertNotNull(hektor);
        assertNotNull(deviceManager);
        assertNotNull(simCardManager);
        return new DefaultUserManager(hektor, deviceManager, simCardManager);
    }

    private DefaultUserManager(final Hektor hektor, final InternalDeviceManager deviceManager, final SimCardManager simCardManager) {
        this.hektor = hektor;
        this.deviceManager = deviceManager;
        this.simCardManager = simCardManager;
        rootUserPath = ActorPath.of("users");
    }

    @Override
    public CompletionStage<Either<Error, User>> addUser(final String name, final User.Profile profile) {
        return simCardManager.createSimCard()
                .thenCompose(either -> map(either, profile.getDeviceType()))
                .thenCompose(either -> map(either, name, profile));
    }

    private CompletionStage<Either<Error, Device>> map(final Either<Error, SimCard> either, final Device.Type type) {
        return either.fold(
                error -> CompletableFuture.completedFuture(Either.left(error)),
                simCard -> deviceManager.createDevice(type, simCard));
    }

    private CompletionStage<Either<Error, User>> map(final Either<Error, Device> either, final String name, final User.Profile profile) {
        return either.fold(
                error -> CompletableFuture.completedFuture(Either.left(error)),
                device -> createUser(name, profile, device));
    }

    private CompletionStage<Either<Error, User>> createUser(final String name, final User.Profile profile, final Device device) {

        final Function<ActorRef, UserContext> ctxFactory = (ref) -> new DefaultUserContext(ref, device);
        final OnStartFunction<UserContext, UserData> startFunction = (actorCtx, ctx, data) -> {
            deviceManager.claim(device.getImei(), actorCtx.self());
            actorCtx.self().tell(UserEvent.TURN_ON);
        };

        final var props = FsmActor.of(AliceFsm.definition)
                .withStartFunction(startFunction)
                .withContext(ctxFactory)
                .withData(() -> new UserData())
                .build();

        final var actorRef = hektor.actorOf(rootUserPath, name, props);
        final var user = new DefaultUser(actorRef, name, profile, device, device.getSimCard());
        return CompletableFuture.completedFuture(Either.right(user));
    }

    private static class DefaultUserContext implements UserContext {

        private final Device device;
        private final ActorRef self;

        private DefaultUserContext(final ActorRef self, final Device device) {
            this.self = self;
            this.device = device;
        }

        @Override
        public Device getDevice() {
            return device;
        }
    }

}
