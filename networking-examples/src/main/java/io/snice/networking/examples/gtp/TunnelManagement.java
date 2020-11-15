package io.snice.networking.examples.gtp;

import io.snice.networking.gtp.PdnSession;

public interface TunnelManagement {

    void onPdnSessionAccepted(PdnSession session);
}
