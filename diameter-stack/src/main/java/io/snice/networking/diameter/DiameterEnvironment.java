package io.snice.networking.diameter;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkStack;
import io.snice.networking.common.IllegalTransportException;
import io.snice.networking.common.Transport;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

public class DiameterEnvironment<C extends DiameterAppConfig> implements Environment<Peer, DiameterMessage, C> {

    private final NetworkStack<Peer, DiameterMessage, C> stack;
    private final C config;

    public DiameterEnvironment(final NetworkStack<Peer, DiameterMessage, C> stack, final C config) {
        this.stack = stack;
        this.config = config;
    }

    @Override
    public C getConfig() {
        return config;
    }

    @Override
    public CompletionStage<Peer> connect(final Transport transport, final InetSocketAddress remoteAddress) throws IllegalTransportException {
        return stack.connect(transport, remoteAddress).thenApply(Peer::of);
    }
}
