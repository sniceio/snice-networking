package io.snice.networking.examples.vplmn.fsm.users.user;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;

import static io.snice.networking.examples.vplmn.fsm.users.user.UserState.DONE;
import static io.snice.networking.examples.vplmn.fsm.users.user.UserState.IDLE;
import static io.snice.networking.examples.vplmn.fsm.users.user.UserState.INIT;
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
        final var init = builder.withInitialState(INIT);
        final var idle = builder.withState(IDLE);
        final var surfing = builder.withState(SURFING);
        final var offline = builder.withState(OFFLINE);
        final var done = builder.withFinalState(DONE);

        init.transitionTo(IDLE).onEvent(UserEvent.TurnOn.class);
        idle.transitionTo(SURFING).onEvent(UserEvent.Surf.class);
        idle.transitionTo(DONE).onEvent(UserEvent.Bye.class);

        surfing.transitionTo(IDLE).onEvent(UserEvent.Idle.class);

        idle.transitionTo(OFFLINE).onEvent(UserEvent.TurnOff.class);
        surfing.transitionTo(OFFLINE).onEvent(UserEvent.TurnOff.class);

        offline.transitionTo(IDLE).onEvent(UserEvent.TurnOn.class);

        definition = builder.build();
    }
}
