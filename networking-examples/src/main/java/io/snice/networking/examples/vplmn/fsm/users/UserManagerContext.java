package io.snice.networking.examples.vplmn.fsm.users;

import io.hektor.actors.fsm.FsmActorContextSupport;
import io.hektor.fsm.Context;
import io.snice.networking.examples.vplmn.DeviceManager;
import io.snice.networking.examples.vplmn.SimCardManager;

public interface UserManagerContext extends Context, FsmActorContextSupport {

    DeviceManager getDeviceManager();

    SimCardManager getSimCardManager();

}
