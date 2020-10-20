package io.snice.networking.diameter.peer;

import io.snice.buffer.Buffers;
import io.snice.buffer.WritableBuffer;
import io.snice.codecs.codec.diameter.DiameterAnswer;
import io.snice.codecs.codec.diameter.DiameterHeader;
import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.codecs.codec.diameter.avp.api.*;
import io.snice.codecs.codec.diameter.avp.type.IpAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class DiameterTestBase {

    protected static DestinationRealm defaultDestinationRealm = DestinationRealm.of("hss.epc.mnc001.mcc001.3gppnetwork.org");
    protected static DestinationHost defaultDestinationHost = DestinationHost.of("epc.mnc001.mcc001.3gppnetwork.org");

    protected static OriginRealm defaultOriginRealm = OriginRealm.of("epc.mnc001.mcc001.3gppnetwork.org");
    protected static OriginHost defaultOriginHost = OriginHost.of("unit.test.node.epc.mnc001.mcc001.3gppnetwork.org");
    protected static HostIpAddress defaultHostIpAddress = HostIpAddress.of(IpAddress.createIpv4Address("10.11.12.13"));
    protected static ProductName defaultProductName = ProductName.of("Snice Unit Test Product");

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    public static DiameterRequest someUlr() {
        final WritableBuffer b = WritableBuffer.of(4);
        b.fastForwardWriterIndex();
        b.setBit(3, 1, true);
        b.setBit(3, 2, true);
        return DiameterRequest.createULR()
                .withAvp(defaultHostIpAddress)
                .withSessionId("asedfasdfasdf")
                .withUserName("999992134354")
                .withDestinationHost(defaultDestinationHost)
                .withDestinationRealm(defaultDestinationRealm)
                .withOriginRealm(defaultOriginRealm)
                .withOriginHost(defaultOriginHost)
                .withAvp(VisitedPlmnId.of(Buffers.wrap("999001")))
                .withAvp(AuthSessionState.NoStateMaintained)
                .withAvp(RatType.Eutran)
                .withAvp(UlrFlags.of(b.build()))
                .build();
    }

    public static DiameterRequest someCer() {
        return DiameterRequest.createCER()
                .withAvp(defaultHostIpAddress)
                .withOriginRealm(defaultOriginRealm)
                .withOriginHost(defaultOriginHost)
                .build();
    }

    public static DiameterAnswer someCea(final ResultCode code) {
        return someCea(code, defaultHostIpAddress, defaultProductName);
    }

    public static DiameterAnswer someCea(final ResultCode code, final HostIpAddress address, final ProductName productName) {
        final var header = DiameterHeader.of().isAnswer().withCommandCode(257);
        return DiameterAnswer.withResultCode(code)
                .withDiameterHeader(header)
                .withAvp(address)
                .withAvp(productName)
                .withOriginHost(defaultOriginHost)
                .withOriginRealm(defaultOriginRealm)
                .build();
    }
}
