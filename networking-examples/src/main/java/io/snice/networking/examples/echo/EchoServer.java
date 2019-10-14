package io.snice.networking.examples.echo;

import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.app.NetworkAppConfig;
import io.snice.networking.app.NetworkApplication;

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
public class EchoServer extends NetworkApplication<String, NetworkAppConfig> {

    public EchoServer() {
        super(String.class);
    }

    @Override
    public void initialize(NetworkBootstrap<String, NetworkAppConfig> bootstrap) {
        bootstrap.onConnection(con -> true).accept(builder -> {
            builder.match(s -> s.startsWith("hello")).consume((connection, str) -> connection.send("hello world!"));
            builder.match(s -> true).consume((c, str) -> c.send(str));
        });
    }

    public static void main(String... args) throws Exception {
        new EchoServer().run(args);
    }
}
