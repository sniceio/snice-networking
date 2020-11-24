package io.snice.networking.gtp.fsm;

import io.snice.codecs.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.codecs.codec.gtp.gtpc.v2.Impl.Gtp2MessageBuilder;
import io.snice.networking.common.fsm.NetworkContext;
import io.snice.networking.gtp.event.GtpEvent;

public interface GtpTunnelContext extends NetworkContext<GtpEvent> {

    void sendDownstream(Gtp2Message msg);

    void sendUpstream(Gtp2Message msg);
}
