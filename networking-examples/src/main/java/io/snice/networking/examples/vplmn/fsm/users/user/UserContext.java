package io.snice.networking.examples.vplmn.fsm.users.user;

import io.hektor.actors.fsm.FsmActorContextSupport;
import io.hektor.fsm.Context;
import io.snice.networking.examples.vplmn.Device;

import java.time.Duration;

public interface UserContext extends Context, FsmActorContextSupport {

    /**
     * For how long are we staying in the IDLE state before transitioning to the next?
     * Which the next state is depends on the underlying FSM. E.g., the {@link AliceFsm}
     * will start "surfing".
     *
     * @return the {@link Duration} in which we'll stay idle.
     */
    default Duration getIdleTime() {
        return Duration.ofMillis(500);
    }

    Device getDevice();
}
