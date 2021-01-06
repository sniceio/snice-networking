package io.snice.networking.examples.buffer;

import io.snice.buffer.Buffers;
import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.app.buffer.BufferApplication;
import io.snice.networking.app.buffer.BufferEnvironment;
import io.snice.networking.bundles.buffer.BufferConnection;
import io.snice.networking.bundles.buffer.BufferEvent;
import io.snice.networking.common.Transport;

/**
 * This is a simple echo server that deals with Strings. I.e., it expects to receive
 * {@link String} from the underlying networking stack and expects to be able to
 * send strings back to the remote party.
 * <p>
 * Also, it does slightly more than just echo:ing back the same string it got. During the
 * {@link NetworkApplication#initialize(NetworkBootstrap)} phase, it sets up some additional
 * rules in order to lookout for some basic keywords, such as "hello" etc and only if
 * it finds to match, it has a default match-all that does the echo part.
 */
public class BufferServer extends BufferApplication<BufferEchoConfig> {

    @Override
    public void initialize(final NetworkBootstrap<BufferConnection, BufferEvent, BufferEchoConfig> bootstrap) {
        bootstrap.onConnection(con -> true).save(c -> {
            // save the connection if we want to re-use the same one
            // without having to ask to it to be re-established (note: under the hood, it may still
            // be around and re-used but from an application point-of-view, you would have to "fetch" it again
            // by asking to have it re-established)
        }).accept(builder -> {
            builder.match(evt -> evt.getBuffer().toString().startsWith("hello")).consume((connection, buffer) -> {
                connection.send(Buffers.wrap("hello world!\n"));
            });
            builder.match(s -> true).map(BufferEvent::getBuffer).consume((c, buffer) -> c.send(buffer));
        });
    }

    @Override
    public void run(final BufferEchoConfig configuration, final BufferEnvironment<BufferEchoConfig> environment) {
        final var t = new Thread(() -> {
            try {
                System.err.println("Connecting");
                final var future = environment.connect(Transport.udp, "127.0.0.1", 4321);
                future.thenAccept(c -> {
                    System.err.println("I mean, it is completed correctly");
                    // c.send(Buffers.wrap("hello"));
                });
            } catch (final Throwable th) {
                th.printStackTrace();
            }
        });
        t.start();

        // sub-classes may override this method in order to setup additional
        // resources etc as the application starts running.
    }

    public static void main(final String... args) throws Exception {
        new BufferServer().run("BufferEchoServerConfig.yml");
    }
}
