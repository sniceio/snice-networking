package io.snice.networking.examples.echo;

import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;

/**
 * This is a simple echo server that deals with Strings. I.e., it expects to receive
 * {@link String} from the underlying networking stack and expects to be able to
 * send strings back to the remote party.
 *
 * Also, it does slightly more than just echo:ing back the same string it got. During the
 * {@link NetworkApplication#initialize(NetworkBootstrap)} phase, it sets up some additional
 * rules in order to lookout for some basic keywords, such as "hello" etc and only if
 * it finds to match, it has a default match-all that does the echo part.
 *
 */
public class EchoClient extends NetworkApplication<Connection<String>, String, EchoClientConfig> {

    public EchoClient() {
        super(String.class, (Class<Connection<String>>)null);
    }

    @Override
    public void run(final EchoClientConfig config, final Environment<Connection<String>, String, EchoClientConfig> environment) {
        environment.connect(Transport.udp, config.getEchoServerIp(), config.getEchoServerPort())
                .thenAccept(connection -> {
                    connection.send("hello");
                });
    }

    @Override
    public void initialize(final NetworkBootstrap<Connection<String>, String, EchoClientConfig> bootstrap) {

        // we are just a client so we will drop any connection attempts made to us.
        bootstrap.onConnection(con -> true).accept(builder -> {
            builder.match(s -> true).consume(System.out::println);
        });
    }

    public static void main(final String... args) throws Exception {
        new EchoClient().run(args);
    }
}
