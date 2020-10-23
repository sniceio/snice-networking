package io.snice.networking.examples.diameter;

import io.netty.util.NetUtil;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.avp.api.ApnOiReplacement;
import io.snice.codecs.codec.diameter.avp.api.DsaFlags;
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
            b.match(DiameterEvent::isAIR).map(DiameterEvent::getRequest).consume(HssApp::processAIR);
            b.match(DiameterEvent::isULA).map(DiameterEvent::getAnswer).consume(HssApp::processULA);
        });
    }

    @Override
    public void run(final HssConfig configuration, final DiameterEnvironment<HssConfig> environment) {

        final var t = new Thread(null, () -> {
            final var c1 = HssHelpers.createPeerConf(3868);
            final var peer = environment.addPeer(c1);
            peer.establishPeer().thenAccept(p -> {
                final var ulr = createULR();
                System.out.println("Sending via a Transaction");
                // p.send(ulr);
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
        final var ula = ulr.createAnswer(ResultCode.DiameterSuccess2001);
        peerConnection.send(ula);
    }

    private static final void processAIR(final PeerConnection peerConnection, final DiameterMessage air) {
        logger.info("Processing a AIR - sending AIA back");
        final var aia = air.createAnswer(ResultCode.DiameterSuccess2001)
                .withAvp(ApnOiReplacement.of("hello.apn.mcc123.mcc123.gprs"))
                .withAvp(DsaFlags.of(123L));
        peerConnection.send(aia);
    }

    private static final void processULA(final PeerConnection peerConnection, final DiameterMessage ula) {
        logger.info("yay, we got a ULA back!");
    }

    public static void main(final String... args) throws Exception {
        System.out.println("Is IPv4 stack preferred? " + NetUtil.isIpV4StackPreferred());

        final var hss = new HssApp();
        hss.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/Hss.yml");
    }

}
