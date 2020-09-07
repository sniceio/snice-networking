package io.snice.networking.examples.diameter;

import io.snice.buffer.Buffers;
import io.snice.buffer.WritableBuffer;
import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.codecs.codec.diameter.avp.api.AuthSessionState;
import io.snice.codecs.codec.diameter.avp.api.RatType;
import io.snice.codecs.codec.diameter.avp.api.UlrFlags;
import io.snice.codecs.codec.diameter.avp.api.VisitedPlmnId;
import io.snice.networking.diameter.peer.PeerConfiguration;

import java.net.URI;
import java.net.URISyntaxException;

public class HssHelpers {

    public static PeerConfiguration createPeerConf(final int port) {
        final var c = new PeerConfiguration();
        try {
            c.setUri(new URI("aaa://10.36.10.77:" + port));
        } catch (final URISyntaxException e) {
            // ignore
        }
        return c;
    }

    public static DiameterRequest createULR() {
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

    public static void sleepy(final int ms) {
        try {
            Thread.sleep(ms);
        } catch (final Exception e) {
        }
    }

}
