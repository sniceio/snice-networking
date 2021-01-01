package io.snice.networking.examples.vplmn;

import io.snice.functional.Either;
import io.snice.networking.examples.vplmn.impl.DefaultSimCardManager;

import java.util.concurrent.CompletionStage;

public interface SimCardManager {

    static SimCardManager of() {
        return DefaultSimCardManager.of();
    }

    CompletionStage<Either<Error, SimCard>> createSimCard();

}
