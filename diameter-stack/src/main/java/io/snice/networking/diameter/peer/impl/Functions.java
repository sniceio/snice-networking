package io.snice.networking.diameter.peer.impl;

import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.common.event.MessageIOEvent;

public final class Functions {

    public static final boolean isCER(final MessageIOEvent event) {
        return ((MessageIOEvent<DiameterMessage>) event).getMessage().isCER();
    }
}
