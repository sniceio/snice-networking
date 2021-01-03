package io.snice.networking.examples.vplmn.fsm.users.user;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;
import io.snice.networking.examples.gtp.Sgw2;
import io.snice.networking.examples.vplmn.fsm.devices.device.DeviceEvent;

import java.time.Duration;

import static io.snice.networking.examples.vplmn.fsm.users.user.UserState.BOOTING;
import static io.snice.networking.examples.vplmn.fsm.users.user.UserState.DONE;
import static io.snice.networking.examples.vplmn.fsm.users.user.UserState.IDLE;
import static io.snice.networking.examples.vplmn.fsm.users.user.UserState.OFFLINE;
import static io.snice.networking.examples.vplmn.fsm.users.user.UserState.SURFING;

/**
 * The persona Alice represents an average iPhone user, who uses her phone for
 * texting and using apps (as in, using data). Her phone is on 24/7 unless she forgot
 * to charge it and as such, it goes offline.
 */
public class AliceFsm {

    public static final Definition<UserState, UserContext, UserData> definition;

    static {
        final var builder = FSM.of(UserState.class).ofContextType(UserContext.class).withDataType(UserData.class);
        final var offline = builder.withInitialState(OFFLINE);
        final var booting = builder.withState(BOOTING);
        final var idle = builder.withState(IDLE);
        final var surfing = builder.withState(SURFING);
        final var done = builder.withFinalState(DONE);

        /**
         * The user tries to boot up their device. If successful,
         * we'll end up in the IDLE state.
         */
        offline.transitionTo(BOOTING).onEvent(UserEvent.TurnOn.class).withAction(AliceFsm::onBoot);

        /**
         * If our device says it came online, then we are good to go and
         * can use our device.
         */
        booting.transitionTo(IDLE).onEvent(DeviceEvent.Online.class);

        idle.withEnterAction(AliceFsm::onEnterIdle);
        idle.transitionTo(SURFING).onEvent(UserEvent.Surf.class).withAction(AliceFsm::onSurf);
        idle.transitionTo(DONE).onEvent(UserEvent.Bye.class);

        surfing.transitionTo(IDLE).onEvent(UserEvent.Surf.class).withGuard(UserEvent.Surf::surfsUp);
        surfing.transitionTo(SURFING).onEvent(UserEvent.Surf.class).withAction(AliceFsm::onSurf);
        surfing.transitionTo(IDLE).onEvent(UserEvent.Idle.class);

        idle.transitionTo(OFFLINE).onEvent(UserEvent.TurnOff.class);
        surfing.transitionTo(OFFLINE).onEvent(UserEvent.TurnOff.class);

        offline.transitionTo(IDLE).onEvent(UserEvent.TurnOn.class);

        definition = builder.build();
    }

    /**
     * When Alice enter's idle, she stays IDLE for
     *
     * @param ctx
     * @param data
     */
    private static void onEnterIdle(final UserContext ctx, final UserData data) {
        ctx.getScheduler().schedule(new UserEvent.Surf(1, Duration.ofMillis(100)), ctx.getIdleTime());
    }

    private static void onBoot(final UserEvent.TurnOn evt, final UserContext ctx, final UserData data) {
        ctx.getDevice().goOnline();
    }

    private static void onSurf(final UserEvent.Surf surf, final UserContext ctx, final UserData data) {
        ctx.getDevice().sendData(Sgw2.dnsQuery, "165.227.89.76", 53);
        ctx.getScheduler().schedule(surf.decrement(), surf.getTimeInBetween());
    }
}
