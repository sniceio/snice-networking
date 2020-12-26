package io.snice.networking.examples.vplmn.fsm;

import io.hektor.actors.fsm.FsmActorContextSupport;
import io.hektor.fsm.Context;
import io.snice.networking.examples.vplmn.Device;

public interface DeviceManagerContext extends Context, FsmActorContextSupport {

    Device createDevice(String imei);


}
