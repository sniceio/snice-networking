package io.snice.networking.diameter;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.common.Connection;

public interface Peer extends Connection<DiameterMessage> {
}
