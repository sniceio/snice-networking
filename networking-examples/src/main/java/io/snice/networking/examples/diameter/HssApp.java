package io.snice.networking.examples.diameter;

import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.avp.api.ExperimentalResultCode;
import io.snice.codecs.codec.diameter.avp.api.ResultCode;
import io.snice.networking.diameter.DiameterApplication;
import io.snice.networking.diameter.DiameterBootstrap;
import io.snice.networking.diameter.DiameterEnvironment;
import io.snice.networking.diameter.PeerConnection;
import io.snice.networking.diameter.event.DiameterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.snice.networking.examples.diameter.HssHelpers.createULR;

public class HssApp extends DiameterApplication<HssConfig> {

    private static final Logger logger = LoggerFactory.getLogger(Hss.class);

    @Override
    public void initialize(final DiameterBootstrap<HssConfig> bootstrap) {
        // bootstrap.onConnection(c -> c.isTCP()).drop();

        bootstrap.onConnection(c -> true).accept(b -> {
            b.match(DiameterEvent::isULR).map(DiameterEvent::getRequest).consume(HssApp::processULR);
            b.match(DiameterEvent::isULA).map(DiameterEvent::getAnswer).consume(HssApp::processULA);
        });
    }

    @Override
    public void run(final HssConfig configuration, final DiameterEnvironment<HssConfig> environment) {

        final var t = new Thread(null, () -> {
            final var c1 = HssHelpers.createPeerConf(3869);
            final var peer = environment.addPeer(c1);
            peer.establishPeer().thenAccept(p -> {
                final var ulr = createULR();
                System.out.println("Sending via a Transaction");
                final var transaction = p.createNewTransaction(ulr)
                        .onAnswer((t2, answer) -> {
                            System.out.println("Got back an Answer in the transaction callback!");
                        })
                        .start();
            });
        }, "kicking off something");
        t.start();
    }

    private static final void processULR(final PeerConnection peerConnection, final DiameterMessage ulr) {
        logger.info("Processing a ULR - sending ULA back");
        final var ula = ulr.createAnswer(ResultCode.DiameterErrorUserUnknown5032)
                // .withDestinationHost(ulr.getOriginHost())
                .withAvp(ExperimentalResultCode.DiameterErrorUserUnknown5001);
        peerConnection.send(ula);
    }

    private static final void processULA(final PeerConnection peerConnection, final DiameterMessage ula) {
        logger.info("yay, we got a ULA back!");
    }

    public static void main(final String... args) throws Exception {
        final var hss = new HssApp();
        hss.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/Hss.yml");
    }

}
