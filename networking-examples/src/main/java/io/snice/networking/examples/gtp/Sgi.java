package io.snice.networking.examples.gtp;

import io.snice.buffer.Buffer;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.app.buffer.BufferApplication;
import io.snice.networking.bundles.buffer.BufferConnection;
import io.snice.networking.bundles.buffer.BufferEvent;
import io.snice.networking.bundles.buffer.BufferReadEvent;
import io.snice.networking.bundles.buffer.BufferWriteEvent;
import io.snice.networking.common.Connection;

import static io.snice.networking.app.NetworkBootstrap.ACCEPT_ALL;

public class Sgi extends BufferApplication<SgiConfig> {

    @Override
    public void initialize(NetworkBootstrap<BufferConnection, BufferEvent, SgiConfig> bootstrap) {
        bootstrap.onConnection(ACCEPT_ALL).accept(b -> {
            b.match(buffer -> true).map(BufferEvent::toReadEvent).consume(Sgi::processBuffer);
        });
    }

    private static void processBuffer(final BufferConnection connection, final BufferReadEvent msg) {
        System.err.println("yay, got a UDP packet back");
    }

}
