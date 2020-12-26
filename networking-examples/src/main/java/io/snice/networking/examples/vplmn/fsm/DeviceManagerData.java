package io.snice.networking.examples.vplmn.fsm;

import io.hektor.fsm.Data;
import io.snice.networking.examples.vplmn.Device;

import java.util.HashMap;
import java.util.Map;

public class DeviceManagerData implements Data {

    private Map<String, Device> devices = new HashMap<>();

    public void storeDevice(final Device device) {
        devices.put(device.getImei(), device);
    }

    public boolean hasDevice(final Device device) {
        return hasDevice(device.getImei());
    }

    public boolean hasDevice(final String imei) {
        return devices.containsKey(imei);
    }
}
