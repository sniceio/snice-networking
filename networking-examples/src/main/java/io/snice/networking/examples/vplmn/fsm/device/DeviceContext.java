package io.snice.networking.examples.vplmn.fsm.device;

import io.hektor.actors.fsm.FsmActorContextSupport;
import io.hektor.fsm.Context;
import io.snice.codecs.codec.gtp.gtpc.v2.messages.tunnel.CreateSessionRequest;

public interface DeviceContext extends Context, FsmActorContextSupport {

    String getImei();

    DeviceConfiguration getConfiguration();

    void initiatePdnSession(CreateSessionRequest csr);

}
