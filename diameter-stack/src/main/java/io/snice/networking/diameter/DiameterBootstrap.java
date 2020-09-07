package io.snice.networking.diameter;

import io.snice.networking.app.ConnectionContext;
import io.snice.networking.common.ConnectionId;
import io.snice.networking.diameter.event.DiameterEvent;

import java.util.function.Predicate;

public interface DiameterBootstrap<C extends DiameterAppConfig> {

    ConnectionContext.Builder<PeerConnection, DiameterEvent, DiameterEvent> onConnection(Predicate<ConnectionId> condition);
}
