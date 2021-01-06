package io.snice.networking.examples.vplmn.fsm.devices;

import io.hektor.actors.fsm.FsmActorContextSupport;
import io.hektor.fsm.Context;
import io.snice.networking.examples.vplmn.Device;
import io.snice.networking.examples.vplmn.SimCard;

public interface DeviceManagerContext extends Context, FsmActorContextSupport {

    /**
     * Ask to have a new {@link Device} created.
     *
     * @param type
     * @param sim
     */
    Device createDevice(Device.Type type, SimCard sim);


}
