package io.snice.networking.examples.vplmn;

import io.hektor.core.Hektor;
import io.snice.functional.Either;
import io.snice.networking.examples.vplmn.impl.DefaultUserManager;

import java.util.concurrent.CompletionStage;

public interface UserManager {

    CompletionStage<Either<Error, User>> addUser(String name, User.Profile profile);

    static UserManager of(final Hektor hektor, final DeviceManager deviceManager, final SimCardManager simCardManager) {
        return DefaultUserManager.of(hektor, deviceManager, simCardManager);
    }

}
