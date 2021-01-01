package io.snice.networking.examples.vplmn.fsm.devices;

import io.snice.networking.examples.vplmn.Device;
import io.snice.networking.examples.vplmn.SimCard;

public interface DeviceManagerEvent {

    class CreateDeviceRequest {
        public final Device.Type type;
        public final SimCard sim;
        public CreateDeviceRequest(final Device.Type type, final SimCard sim) {
            this.type = type;
            this.sim = sim;
        }
    }

}
