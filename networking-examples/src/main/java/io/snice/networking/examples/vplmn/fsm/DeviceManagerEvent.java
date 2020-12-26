package io.snice.networking.examples.vplmn.fsm;

import static io.snice.preconditions.PreConditions.assertNotEmpty;

public abstract class DeviceManagerEvent {


    public static ManageDeviceEvent manageDevice(final String imei) {
        assertNotEmpty(imei);
        return new ManageDeviceEvent(imei);
    }

    public abstract static class DeviceEvent extends DeviceManagerEvent {

        private final String imei;

        private DeviceEvent(final String imei) {
            this.imei = imei;
        }

        public String getImei() {
            return imei;
        }
    }

    public static class ManageDeviceEvent extends DeviceEvent {
        private ManageDeviceEvent(final String imei) {
            super(imei);
        }
    }

}
