package io.snice.networking.examples.gtp;

import io.snice.networking.app.Environment;
import io.snice.networking.app.MessagePipe;
import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.codec.gtp.GtpMessage;
import io.snice.networking.codec.gtp.GtpSerializationFactory;
import io.snice.networking.codec.gtp.gtpc.v2.Gtp2Message;
import io.snice.networking.common.Connection;
import io.snice.networking.common.ConnectionId;

import static io.snice.networking.app.MessagePipe.match;


public class Pgw extends NetworkApplication<GtpMessage, GtpConfig> {

    private static final MessagePipe<Connection<GtpMessage>, Gtp2Message, Gtp2Message> echo;
    private static final MessagePipe<Connection<GtpMessage>, Gtp2Message, Gtp2Message> csr;

    static {
        // for all echo messages, simply reply back

        // TODO: not done yet. Need to actually create the echo response
        echo = match(Pgw::isEcho).consume((c, gtp) -> c.send(gtp));

        // TODO: Not done yet. Need to create a create session response
        csr = match(Pgw::isCSR).consume((c, gtp) -> c.send(gtp));
    }

    public static boolean isEcho(final Connection<GtpMessage> c, final Gtp2Message gtp) {
        return gtp.isEchoRequest();
    }

    public static boolean isCSR(final Connection<GtpMessage> c, final Gtp2Message gtp) {
        return gtp.isCreateSessionRequest();
    }


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

            // no GTPv1 right now so just drop it...
            builder.match(GtpMessage::isGtpVersion1).consume((c, gtp) -> c.close());

            builder.match(GtpMessage::isGtpVersion2).map(GtpMessage::toGtp2Message).withPipe(echo);


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
