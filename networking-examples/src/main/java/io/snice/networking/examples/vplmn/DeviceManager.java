package io.snice.networking.examples.vplmn;

import io.hektor.core.Hektor;
import io.snice.functional.Either;
import io.snice.networking.examples.gtp.GtpConfig;
import io.snice.networking.examples.vplmn.impl.DefaultDeviceManager;
import io.snice.networking.gtp.GtpControlTunnel;
import io.snice.networking.gtp.GtpEnvironment;

import java.util.concurrent.CompletionStage;

public interface DeviceManager {

    CompletionStage<Either<Error, Device>> createDevice(Device.Type type, SimCard simCard);

    static DeviceManager of(final Hektor hektor, final GtpEnvironment<GtpConfig> environment, final GtpControlTunnel tunnel) {
        return DefaultDeviceManager.of(hektor, environment, tunnel);
    }



}
