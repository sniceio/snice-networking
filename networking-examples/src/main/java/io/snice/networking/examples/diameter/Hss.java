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
import io.snice.networking.diameter.peer.PeerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import static io.snice.networking.app.NetworkBootstrap.ACCEPT_ALL;

public class Hss extends NetworkApplication<DiameterEnvironment<HssConfig>, PeerConnection, DiameterMessage, HssConfig> {

    private static final Logger logger = LoggerFactory.getLogger(Hss.class);

    public Hss(final DiameterBundle bundle) {
        super(bundle);
    }

    private void sleepy(final int ms) {
        try {
            Thread.sleep(ms);
        } catch (final Exception e) {
        }
    }

    private PeerConfiguration createPeerConf(final int port) {
        final var c = new PeerConfiguration();
        try {
            c.setUri(new URI("aaa://10.36.10.77:" + port));
        } catch (final URISyntaxException e) {
            // ignore
        }
        return c;
    }

    @Override
    public void run(final HssConfig configuration, final DiameterEnvironment<HssConfig> environment) {

        // peer.establishPeer();

        final var t = new Thread(null, () -> {
            final var c1 = createPeerConf(3869);
            final var c2 = createPeerConf(3870);
            final var peer = environment.addPeer(c1);
            final var peer2 = environment.addPeer(c2);

            System.err.println("About to send ULR but will sleep first");
            sleepy(1000);
            System.err.println("Ok, sending");
            peer.establishPeer().thenAccept(p -> p.send(createULR()));
            peer2.establishPeer().thenAccept(p -> System.err.println("Peer2 estabalished"));

            for (int i = 0; i < 2; ++i) {
                sleepy(100);
                System.err.println("Ok, sending via Peer.send");
                // environment.getPeers().stream().findAny().ifPresent(peer -> peer.send(createULR()));
                peer.send(createULR());
            }

            System.err.println("Ok, sending to both peers now.");
            sleepy(1000);
            for (int i = 0; i < 2; ++i) {
                sleepy(100);
                System.err.println("Ok, sending via Peer.establishPeer().thenAccept");
                // environment.getPeers().stream().findAny().ifPresent(peer -> peer.send(createULR()));
                peer.establishPeer().thenAccept(p -> p.send(createULR()));
                peer2.send(createULR());
            }
        }, "kicking off something");
        // t.start();

        environment.getPeers().forEach(p -> {
            logger.info("Establishing peer: " + p);
            p.establishPeer().thenAccept(established -> {
                logger.info("Peer Established: " + p);
                final var myPeer = established;
                final var th = new Thread(null, () -> {
                    logger.info("Starting new Peer Thread for peer: " + myPeer);
                    myPeer.send(createULR());
                    final var random = new Random();
                    int max = random.nextInt(50);
                    logger.info("Sleeping 5 seconds then issuing " + max + " no of ULRs for Peer " + myPeer);
                    sleepy(5000);
                    for (int i = 0; i < max; ++i) {
                        myPeer.send(createULR());
                        sleepy(5);
                    }
                    int sleepy = random.nextInt(30) + 10;
                    /*
                    max = random.nextInt(50);
                    logger.info("Sleeping " + sleepy + " seconds then issuing " + max + " no of ULRs for Peer " + myPeer);
                    sleepy(sleepy * 1000);
                    for (int i = 0; i < max; ++i) {
                        myPeer.send(createULR());
                        sleepy(5);
                    }
                     */
                    sleepy = 4;
                    max = 10000;
                    logger.info("Performance: Sleeping " + sleepy + " seconds then issuing " + max + " no of ULRs for Peer " + myPeer);
                    for (int i = 0; i < max; ++i) {
                        myPeer.send(createULR());
                        sleepy(2);
                    }
                });

                // th.start();
            });
        });

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
            b.matchEvent(o -> o instanceof String).map(o -> (String) o).consume(s -> {
                System.out.println("I got the String " + s + " which is of length " + s.length());
            });
            b.matchEvent(o -> o instanceof Integer).map(o -> (Integer) o).consume(i -> {
                System.out.println("I got the Integer " + i);
            });
        });

    }

    private static final void processULR(final PeerConnection peerConnection, final DiameterMessage ulr) {
        final var ula = ulr.createAnswer(ResultCode.DiameterErrorUserUnknown5032)
                .withAvp(ExperimentalResultCode.DiameterErrorUserUnknown5001);
        peerConnection.send(ula);
    }

    private static final void processULA(final PeerConnection peerConnection, final DiameterMessage ula) {
        // logger.info("yay, we got a ULA back!");
    }

    public static void main(final String... args) throws Exception {
        final DiameterBundle bundle = new DiameterBundle();
        final var hss = new Hss(bundle);
        hss.run("server", "networking-examples/src/main/resources/io/snice/networking/examples/Hss.yml");
    }
}
