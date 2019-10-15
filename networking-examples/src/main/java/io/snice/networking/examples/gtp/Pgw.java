package io.snice.networking.examples.gtp;

import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.codec.gtp.GtpMessage;
import io.snice.networking.codec.gtp.GtpSerializationFactory;
import io.snice.networking.common.ConnectionId;


public class Pgw extends NetworkApplication<GtpMessage, GtpConfig> {


    public Pgw() {
        super(GtpMessage.class);
    }

    @Override
    public void run(final GtpConfig configuration, final Environment<GtpMessage, GtpConfig> environment) {

    }

    @Override
    public void initialize(final NetworkBootstrap<GtpMessage, GtpConfig> bootstrap) {

        bootstrap.registerSerializationFactory(new GtpSerializationFactory());

        // only accept traffic from localhost, drop the rest.
        bootstrap.onConnection(id -> true).accept(builder -> {
            builder.withDefaultStatisticsModule();
            builder.match(gtp -> gtp.getVersion() == 2 && gtp.getMessageTypeDecimal() == 1).consume(echo -> System.out.println("Got echo request"));
            builder.match(gtp -> gtp.getVersion() == 2 && gtp.getMessageTypeDecimal() == 32).consume(crs -> System.out.println("Got Create Session Request"));
            /*
            builder.match(b -> b.startsWith(hello)).consume((c, h) -> {
                System.err.println("Got a hello...");
                c.send(Buffers.wrap("world"));
            });

            builder.match(b -> true).map(Buffer::toString).consume(s -> System.out.println("cool, this is what i got: " + s));

            builder.match(Buffer::endsWithCRLF).map(b -> "ends with CRLF").consume(System.out::println);
            builder.match(b -> b == null || b.isEmpty()).consume((c, b) -> c.send(Buffers.wrap("empty isn't allowed")));

            builder.match(Buffer::endsWithCRLF).map(Buffer::stripEOL).map(Buffer::toString).map(s -> s.isEmpty()).consume(bool -> {
                if (bool) {
                    System.out.println("Ok, it is true");
                } else {
                    System.out.println("Hmm, not true");
                }
            });
            */
        });

        // drop the rest...
        // bootstrap.onConnection(id -> true).drop();

    }

    private static boolean isFromLocalHost(final ConnectionId id) {
        return "127.0.0.1".equals(id.getRemoteIpAddress());
    }

    public static void main(final String... args) throws Exception {
        final var pgw = new Pgw();
        pgw.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/PGW.yml");
    }
}
