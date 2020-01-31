package io.snice.networking.examples.diameter;

import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.avp.api.ExperimentalResultCode;
import io.snice.networking.codec.diameter.avp.api.ResultCode;
import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.snice.networking.app.NetworkBootstrap.ACCEPT_ALL;

public class Hss extends NetworkApplication<DiameterMessage, HssConfig> {

    private static final Logger logger = LoggerFactory.getLogger(Hss.class);

    public Hss() {
        super(DiameterMessage.class);
    }

    @Override
    public void run(final HssConfig configuration, final Environment<DiameterMessage, HssConfig> environment) {
        final var future = environment.connect(Transport.tcp, "127.0.0.1", 3868);
        future.whenComplete((c, t) -> {
            logger.info("got it");
        });
    }

    @Override
    public void initialize(final NetworkBootstrap<DiameterMessage, HssConfig> bootstrap) {

        bootstrap.onConnection(ACCEPT_ALL).accept(b -> {
            b.match(DiameterMessage::isULR).consume(Hss::processULR);
        });

    }

    private static final void processULR(final Connection<DiameterMessage> con, final DiameterMessage ulr) {
        final var ula = ulr.createAnswer(ResultCode.DiameterSuccess2001)
                .withAvp(ExperimentalResultCode.DiameterErrorUserUnknown5001)
                .withAvp(null)
                .build();
        con.send(ula);
    }

    public static void main(final String... args) throws Exception {
        final var hss = new Hss();
        hss.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/Hss.yml");
    }
}
