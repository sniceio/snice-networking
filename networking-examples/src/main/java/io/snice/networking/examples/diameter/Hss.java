package io.snice.networking.examples.diameter;

import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.DiameterSerializationFactory;

import static io.snice.networking.app.NetworkBootstrap.ACCEPT_ALL;

public class Hss extends NetworkApplication<DiameterMessage, HssConfig> {

    public Hss() {
        super(DiameterMessage.class);
    }

    @Override
    public void initialize(final NetworkBootstrap<DiameterMessage, HssConfig> bootstrap) {

        bootstrap.registerSerializationFactory(new DiameterSerializationFactory());

        bootstrap.onConnection(ACCEPT_ALL).accept(b -> {
            b.match(msg -> true).consume(System.out::println);
        });

    }

    public static void main(final String... args) throws Exception {
        final var hss = new Hss();
        hss.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/Hss.yml");
    }
}
