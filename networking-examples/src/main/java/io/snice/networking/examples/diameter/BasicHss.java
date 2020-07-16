package io.snice.networking.examples.diameter;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.avp.api.ExperimentalResultCode;
import io.snice.codecs.codec.diameter.avp.api.ResultCode;
import io.snice.networking.app.BasicNetworkApplication;
import io.snice.networking.app.Environment;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.bundles.ProtocolBundleRegistry;
import io.snice.networking.common.Connection;
import io.snice.networking.diameter.DiameterBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.snice.networking.app.NetworkBootstrap.ACCEPT_ALL;

/**
 * Really just to get a feel for the {@link BasicNetworkApplication} for those apps
 * that may not care enough...
 */
public class BasicHss extends BasicNetworkApplication<DiameterMessage, HssConfig> {

    private static final Logger logger = LoggerFactory.getLogger(BasicHss.class);

    public BasicHss() {
        super(DiameterMessage.class);
    }

    @Override
    public void run(final HssConfig configuration, final Environment<Connection<DiameterMessage>, DiameterMessage, HssConfig> environment) {
    }

    @Override
    public void initialize(final NetworkBootstrap<Connection<DiameterMessage>, DiameterMessage, HssConfig> bootstrap) {
        bootstrap.onConnection(ACCEPT_ALL).accept(b -> {
            b.match(DiameterMessage::isULR).consume(BasicHss::processULR);
            b.match(DiameterMessage::isULA).consume(BasicHss::processULA);
        });
    }

    private static final void processULR(final Connection<DiameterMessage> con, final DiameterMessage ulr) {
        final var ula = ulr.createAnswer(ResultCode.DiameterErrorUserUnknown5032)
                .withAvp(ExperimentalResultCode.DiameterErrorUserUnknown5001)
                .build();
        con.send(ula);
    }

    private static final void processULA(final Connection<DiameterMessage> con, final DiameterMessage ula) {
        logger.info("yay, we got a ULA back!");
    }

    public static void main(final String... args) throws Exception {
        // TODO: how to deal with this.
        // TODO: perhaps via Java SPI - https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/ServiceLoader.html
        ProtocolBundleRegistry.getDefaultRegistry().registerBundle(new DiameterBundle(), DiameterMessage.class);

        final var hss = new BasicHss();
        hss.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/Hss.yml");
    }
}
