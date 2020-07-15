package io.snice.networking.examples.diameter;

import io.snice.buffer.Buffers;
import io.snice.buffer.WritableBuffer;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.codecs.codec.diameter.avp.api.*;
import io.snice.networking.app.*;
import io.snice.networking.common.Connection;
import io.snice.networking.common.Transport;
import io.snice.networking.diameter.DiameterBundle;
import io.snice.networking.diameter.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.snice.networking.app.NetworkBootstrap.ACCEPT_ALL;

/**
 * Really just to get a feel for the {@link BasicNetworkApplication} for those apps
 * that may not care enough...
 */
public class BasicHss extends BasicNetworkApplication<DiameterMessage, HssConfig> {

    private static final Logger logger = LoggerFactory.getLogger(BasicHss.class);

    public BasicHss(final DiameterBundle bundle) {
        super((AppBundle)bundle);
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
        final var hss = new BasicHss(new DiameterBundle());
        hss.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/Hss.yml");
    }
}
