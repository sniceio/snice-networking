package io.snice.networking.gtp.impl;

import io.snice.codecs.codec.gtp.gtpc.v2.tliv.BearerContext;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.Ebi;
import io.snice.codecs.codec.gtp.gtpc.v2.tliv.FTeid;
import io.snice.networking.gtp.Bearer;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

public class DefaultBearer implements Bearer {

    public static Bearer of(final BearerContext ctx) {
        assertNotNull(ctx, "The bearer context cannot be null");
        final var fteid = getFTeid(ctx).orElseThrow(() -> new IllegalArgumentException("The given BearerContext does not contain an FTEID"));
        return new DefaultBearer(ctx, fteid);
    }
    private final BearerContext ctx;
    private final FTeid fteid;

    private DefaultBearer(final BearerContext ctx, final FTeid fTeid) {
        this.ctx = ctx;
        this.fteid = fTeid;
    }

    @Override
    public Optional<Ebi> getEbi() {
        return ctx.getValue().getInformationElement(Ebi.TYPE, 0)
                .map(tliv -> (Ebi) tliv.ensure());
    }

    @Override
    public FTeid getFTeid() {
        return fteid;
    }

    private static Optional<FTeid> getFTeid(final BearerContext ctx) {
        // TODO: should add this to the actual BearerContext instead.
        return ctx.getValue().getInformationElement(FTeid.TYPE).map(tliv -> (FTeid) (tliv.ensure()));
    }

}
