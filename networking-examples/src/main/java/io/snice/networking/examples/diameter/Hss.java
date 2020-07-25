package io.snice.networking.examples.diameter;

import io.snice.buffer.Buffers;
import io.snice.buffer.WritableBuffer;
import io.snice.codecs.codec.diameter.DiameterMessage;
import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.codecs.codec.diameter.avp.api.*;
import io.snice.networking.app.NetworkApplication;
import io.snice.networking.app.NetworkBootstrap;
import io.snice.networking.diameter.DiameterBundle;
import io.snice.networking.diameter.DiameterEnvironment;
import io.snice.networking.diameter.PeerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.snice.networking.app.NetworkBootstrap.ACCEPT_ALL;

public class Hss extends NetworkApplication<DiameterEnvironment<HssConfig>, PeerConnection, DiameterMessage, HssConfig> {

    private static final Logger logger = LoggerFactory.getLogger(Hss.class);

    public Hss(final DiameterBundle bundle) {
        super(bundle);
    }

    private void sleepy(final int ms) {
        try {
            Thread.sleep(ms);
        } catch (final Exception e) { }
    }
    @Override
    public void run(final HssConfig configuration, final DiameterEnvironment<HssConfig> environment) {
        final var t = new Thread(null, new Runnable() {
            @Override
            public void run() {
                System.err.println("About to send ULR but will sleep first");
                sleepy(1000);
                System.err.println("Ok, sending");
                environment.send(createULR());

                for (int i = 0; i < 2; ++i) {
                    sleepy(100);
                    System.err.println("Ok, sending via the peer");
                    environment.getPeers().stream().findAny().ifPresent(peer -> peer.send(createULR()));
                }
            }
        }, "kicking off something");
        t.start();

        /*
        final var future = environment.connect(Transport.tcp, "10.36.10.77", 3869);
        future.whenComplete((c, t) -> {
            if (c != null) {
                try {
                    c.send(createULR());
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
            } else {
                t.printStackTrace();
            }
        });
         */
    }

    private DiameterRequest createULR() {
        final WritableBuffer b = WritableBuffer.of(4);
        b.fastForwardWriterIndex();
        b.setBit(3, 1, true);
        b.setBit(3, 2, true);
        final var ulr = DiameterRequest.createULR()
                .withSessionId("asedfasdfasdf")
                .withUserName("999992134354")
                .withDestinationHost("hss.epc.mnc001.mcc001.3gppnetwork.org")
                .withDestinationRealm("epc.mnc001.mcc001.3gppnetwork.org")
                .withOriginRealm("epc.mnc999.mcc999.3gppnetwork.org")
                .withOriginHost("snice.node.epc.mnc999.mcc999.3gppnetwork.org")
                .withAvp(VisitedPlmnId.of(Buffers.wrap("999001")))
                .withAvp(AuthSessionState.NoStateMaintained)
                .withAvp(RatType.Eutran)
                .withAvp(UlrFlags.of(b.build()))
                .build();
        return ulr;
    }


    @Override
    public void initialize(final NetworkBootstrap<PeerConnection, DiameterMessage, HssConfig> bootstrap) {
        bootstrap.onConnection(ACCEPT_ALL).accept(b -> {
            b.match(DiameterMessage::isULR).consume(Hss::processULR);
            b.match(DiameterMessage::isULA).consume(Hss::processULA);
        });

    }

    private static final void processULR(final PeerConnection peerConnection, final DiameterMessage ulr) {
        final var ula = ulr.createAnswer(ResultCode.DiameterErrorUserUnknown5032)
                .withAvp(ExperimentalResultCode.DiameterErrorUserUnknown5001);
        peerConnection.send(ula);
    }

    private static final void processULA(final PeerConnection peerConnection, final DiameterMessage ula) {
        logger.info("yay, we got a ULA back!");
    }

    public static void main(final String... args) throws Exception {
        final DiameterBundle bundle = new DiameterBundle();
        final var hss = new Hss(bundle);
        hss.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/Hss.yml");
    }
}
