package io.snice.networking.examples.vplmn.fsm.users;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;

import static io.snice.networking.examples.vplmn.fsm.users.UserManagerState.INIT;
import static io.snice.networking.examples.vplmn.fsm.users.UserManagerState.RUNNING;
import static io.snice.networking.examples.vplmn.fsm.users.UserManagerState.TERMINATED;
import static io.snice.networking.examples.vplmn.fsm.users.UserManagerState.TERMINATING;

public class UserManagerFsm {

    public static final Definition<UserManagerState, UserManagerContext, UserManagerData> definition;

    static {
        final var builder = FSM.of(UserManagerState.class).ofContextType(UserManagerContext.class).withDataType(UserManagerData.class);
        final var init = builder.withInitialState(INIT);
        final var running = builder.withState(RUNNING);
        final var terminating = builder.withState(TERMINATING);
        final var terminated = builder.withFinalState(TERMINATED);

        init.transitionTo(RUNNING).onEvent(UserManagerEvent.Init.class);
        running.transitionTo(TERMINATING).onEvent(UserManagerEvent.Terminate.class);
        running.transitionTo(RUNNING).onEvent(UserManagerEvent.AddUser.class).withAction(UserManagerFsm::addUser);
        terminating.transitionTo(TERMINATED).onEvent(UserManagerEvent.Terminated.class);

        definition = builder.build();
    }

    private static void addUser(final UserManagerEvent.AddUser addUser, final UserManagerContext ctx, final UserManagerData data) {
        final var profile = addUser.profile;
        final var simCard = ctx.getSimCardManager().createSimCard();
        // ctx.getDeviceManager().createDevice(profile.getDeviceType(), simCard);
        // System.err.println("Adding user");
    }
}
