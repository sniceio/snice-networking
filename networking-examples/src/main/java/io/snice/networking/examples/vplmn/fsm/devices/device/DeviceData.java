package io.snice.networking.examples.vplmn.fsm.devices.device;

import io.hektor.fsm.Data;
import io.snice.networking.gtp.EpsBearer;
import io.snice.networking.gtp.PdnSessionContext;

import java.util.Optional;

public class DeviceData implements Data {

    private PdnSessionContext ctx;

    private Optional<EpsBearer> defaultBearer = Optional.empty();

    public void storePdnSession(final PdnSessionContext ctx) {
        this.ctx = ctx;
    }

    public Optional<PdnSessionContext> getPdnSession() {
        return Optional.ofNullable(ctx);
    }

    public void storeEpsBearer(final EpsBearer bearer) {
        // for now we'll assume the given bearer is the
        // default bearer.
        defaultBearer = Optional.of(bearer);
    }

    public Optional<EpsBearer> getDefaultBearer() {
        return defaultBearer;
    }
}
