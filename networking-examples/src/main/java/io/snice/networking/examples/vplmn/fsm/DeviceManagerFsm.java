package io.snice.networking.examples.vplmn.fsm;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;
import io.snice.networking.examples.vplmn.Error;
import io.snice.networking.examples.vplmn.fsm.DeviceManagerEvent.ManageDeviceEvent;

import static io.snice.networking.examples.vplmn.fsm.DeviceManagerState.INIT;
import static io.snice.networking.examples.vplmn.fsm.DeviceManagerState.RUNNING;
import static io.snice.networking.examples.vplmn.fsm.DeviceManagerState.TERMINATED;

public class DeviceManagerFsm {

    public static final Definition<DeviceManagerState, DeviceManagerContext, DeviceManagerData> definition;

    static {
        final var builder = FSM.of(DeviceManagerState.class).ofContextType(DeviceManagerContext.class).withDataType(DeviceManagerData.class);

        final var init = builder.withInitialState(INIT);
        final var running = builder.withState(RUNNING);
        final var terminated = builder.withFinalState(TERMINATED);

        init.transitionTo(RUNNING).onEvent(String.class).withGuard("run"::equals);

        running.transitionTo(RUNNING).onEvent(ManageDeviceEvent.class)
                .withGuard((evt, ctx, data) -> data.hasDevice(evt.getImei()))
                .withAction((evt, ctx, data) -> {
                    ctx.sender().tell(Error.of("Device with IMEI " + evt.getImei() + " already exists"));
                });

        running.transitionTo(RUNNING).onEvent(ManageDeviceEvent.class)
                .withAction((evt, ctx, data) -> {
                    final var device = ctx.createDevice(evt.getImei());
                    data.storeDevice(device);
                    ctx.sender().tell(device);
                });

        running.transitionTo(TERMINATED).onEvent(String.class).withGuard("terminate"::equals);

        definition = builder.build();
    }

}
