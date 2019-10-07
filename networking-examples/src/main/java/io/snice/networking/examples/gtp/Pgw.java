package io.snice.networking.examples.gtp;

import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.networking.app.Bootstrap;
import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkApplication;
import io.snice.networking.common.ConnectionId;


public class Pgw extends NetworkApplication<GtpConfig> {

    @Override
    public void run(final GtpConfig configuration, final Environment environment) {

    }

    @Override
    public void initialize(final Bootstrap<GtpConfig> bootstrap) {

        final var hello = Buffers.wrap("hello");

        // only accept traffic from localhost, drop the rest.
        bootstrap.onConnection(Pgw::isFromLocalHost).accept(builder -> {
            builder.withDefaultStatisticsModule();
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
        });

        // drop the rest...
        bootstrap.onConnection(id -> true).drop();

    }

    private static boolean isFromLocalHost(final ConnectionId id) {
        return "127.0.0.1".equals(id.getRemoteIpAddress());
    }

    public static void main(final String... args) throws Exception {
        final var pgw = new Pgw();
        pgw.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/PGW.yml");
    }
}
