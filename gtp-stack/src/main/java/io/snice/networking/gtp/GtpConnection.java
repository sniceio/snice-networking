package io.snice.networking.gtp;

import io.snice.codecs.codec.gtp.GtpMessage;
import io.snice.networking.common.Connection;
import io.snice.networking.gtp.event.GtpEvent;

public interface GtpConnection extends Connection<GtpEvent> {

    void send(GtpMessage msg);
}
