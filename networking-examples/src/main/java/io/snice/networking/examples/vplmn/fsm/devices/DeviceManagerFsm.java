package io.snice.networking.examples.vplmn.fsm.devices;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;
import io.snice.networking.examples.vplmn.fsm.devices.DeviceManagerEvent.CreateDeviceRequest;

import static io.snice.networking.examples.vplmn.fsm.devices.DeviceManagerState.INIT;
import static io.snice.networking.examples.vplmn.fsm.devices.DeviceManagerState.RUNNING;
import static io.snice.networking.examples.vplmn.fsm.devices.DeviceManagerState.TERMINATED;

public class DeviceManagerFsm {

    public static final Definition<DeviceManagerState, DeviceManagerContext, DeviceManagerData> definition;

    static {
        final var builder = FSM.of(DeviceManagerState.class).ofContextType(DeviceManagerContext.class).withDataType(DeviceManagerData.class);

        final var init = builder.withInitialState(INIT);
        final var running = builder.withState(RUNNING);
        final var terminated = builder.withFinalState(TERMINATED);

        init.transitionTo(RUNNING).onEvent(String.class).withGuard("run"::equals);


        running.transitionTo(RUNNING).onEvent(CreateDeviceRequest.class)
                .withAction((evt, ctx, data) -> {
                    // final var device = ctx.createDevice(evt.type, evt.sim);
                    // data.storeDevice(device);
                    // ctx.sender().tell(device);
                });

        running.transitionTo(TERMINATED).onEvent(String.class).withGuard("terminate"::equals);

        definition = builder.build();
    }

}
