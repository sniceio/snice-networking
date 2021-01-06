package io.snice.networking.examples.buffer;

import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.app.buffer.BufferApplication;
import io.snice.networking.app.buffer.BufferEnvironment;
import io.snice.networking.bundles.buffer.BufferConnection;
import io.snice.networking.bundles.buffer.BufferEvent;
import io.snice.networking.common.Transport;
import io.snice.networking.examples.echo.EchoClientConfig;

import static io.snice.networking.app.NetworkBootstrap.ACCEPT_ALL;

public class BufferClient extends BufferApplication<EchoClientConfig> {

    @Override
    public void initialize(final NetworkBootstrap<BufferConnection, BufferEvent, EchoClientConfig> bootstrap) {
        /*
        bootstrap.onConnection(ACCEPT_ALL).accept(b -> {
            b.match(evt -> true).consume(evt -> System.err.println("Received: " + evt.getBuffer()));
        });

         */
        bootstrap.onConnection(ACCEPT_ALL).drop(c -> {
            // TODO: this isn't actually working right now. This function isn't being executed
            // but the connection is dropped correctly
            System.err.println("Dropping connection because I'm a client only. Remote: " + c.id().getRemoteConnectionEndpointId());
            return null;
        });
    }

    @Override
    public void run(final EchoClientConfig config, final BufferEnvironment<EchoClientConfig> environment) {
        environment.connect(Transport.udp, config.getEchoServerIp(), config.getEchoServerPort())
                .thenAccept(connection -> connection.send("hello\n\r"));
    }

    public static void main(final String... args) throws Exception {
        new BufferClient().run("EchoClientConfig.yml");
    }
}
