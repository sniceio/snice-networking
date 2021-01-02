package io.snice.networking.examples.vplmn.impl;

import io.hektor.core.ActorRef;
import io.snice.codecs.codec.Imei;
import io.snice.networking.examples.vplmn.DeviceManager;

public interface InternalDeviceManager extends DeviceManager {

    void claim(Imei imei, ActorRef owner);
}
