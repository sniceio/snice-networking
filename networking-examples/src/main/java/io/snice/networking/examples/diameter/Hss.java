package io.snice.networking.examples.diameter;

import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.codec.diameter.DiameterMessage;
import io.snice.networking.codec.diameter.DiameterRequest;
import io.snice.networking.codec.diameter.avp.api.ExperimentalResultCode;
import io.snice.networking.codec.diameter.avp.api.ResultCode;
import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;
import io.snice.networking.diameter.DiameterBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.snice.networking.app.NetworkBootstrap.ACCEPT_ALL;

public class Hss extends NetworkApplication<DiameterMessage, HssConfig> {

    private static final Logger logger = LoggerFactory.getLogger(Hss.class);

    public Hss(final DiameterBundle bundle) {
        super(bundle);
    }

    @Override
    public void run(final HssConfig configuration, final Environment<DiameterMessage, HssConfig> environment) {
        if (false) return;
        final var future = environment.connect(Transport.tcp, "10.36.10.74", 3868);
        // final var future = environment.connect(Transport.tcp, "127.0.0.1", 3869);
        future.whenComplete((c, t) -> {
            if (c != null) {
                final var ulr = DiameterRequest.createULR()
                        .withUserName("999992134354")
                        .withDestinationHost("hss.epc.mnc001.mcc001.3gppnetwork.org")
                        .withDestinationRealm("epc.mnc001.mcc001.3gppnetwork.org")
                        .withOriginHost("snice.node.mnc999.mcc999.3gpp")
                        .withSessionId("asedfasdfasdf")
                        .build();
                c.send(ulr);
            }
        });
    }

    @Override
    public void initialize(final NetworkBootstrap<DiameterMessage, HssConfig> bootstrap) {

        bootstrap.onConnection(ACCEPT_ALL).accept(b -> {
            b.match(DiameterMessage::isULR).consume(Hss::processULR);
            b.match(DiameterMessage::isULA).consume(Hss::processULA);
        });

    }

    private static final void processULR(final Connection<DiameterMessage> con, final DiameterMessage ulr) {
        final var ula = ulr.createAnswer(ResultCode.DiameterErrorUserUnknown5032)
                .withAvp(ExperimentalResultCode.DiameterErrorUserUnknown5001)
                .withAvp(null)
                .build();
        con.send(ula);
    }

    private static final void processULA(final Connection<DiameterMessage> con, final DiameterMessage ula) {
        logger.info("yay, we got a ULA back!");
    }

    public static void main(final String... args) throws Exception {
        final DiameterBundle bundle = new DiameterBundle();
        final var hss = new Hss(bundle);
        hss.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/Hss.yml");
    }
}
