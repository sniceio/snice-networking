package io.snice.networking.examples.vplmn.impl;

import io.snice.codecs.codec.Iccid;
import io.snice.codecs.codec.Imsi;
import io.snice.codecs.codec.MccMnc;
import io.snice.functional.Either;
import io.snice.networking.examples.vplmn.Error;
import io.snice.networking.examples.vplmn.SimCard;
import io.snice.networking.examples.vplmn.SimCardManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultSimCardManager implements SimCardManager {

    private final MccMnc vplmn = MccMnc.of("001", "001");
    private final AtomicInteger imsiCount = new AtomicInteger(0);
    private final AtomicLong iccidCount = new AtomicLong(0);

    public static SimCardManager of() {
        return new DefaultSimCardManager();
    }

    private DefaultSimCardManager() {

    }

    @Override
    public CompletionStage<Either<Error, SimCard>> createSimCard() {
        final var iccid = Iccid.of(iccidCount.incrementAndGet());
        final var imsi = Imsi.of(vplmn, imsiCount.incrementAndGet());
        final var simCard = new SingleImsiSimCard(iccid, imsi);
        return CompletableFuture.completedFuture(Either.right(simCard));
    }
}
