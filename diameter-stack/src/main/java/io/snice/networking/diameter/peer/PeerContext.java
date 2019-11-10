package io.snice.networking.diameter.peer;

import io.hektor.fsm.Context;
import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.common.fsm.NetworkContext;


public interface PeerContext extends NetworkContext<DiameterMessage> {

}
