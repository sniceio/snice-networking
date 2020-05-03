package io.snice.networking.diameter.peer;

import io.snice.codecs.codec.diameter.DiameterAnswer;
import io.snice.codecs.codec.diameter.DiameterHeader;
import io.snice.codecs.codec.diameter.DiameterRequest;
import io.snice.codecs.codec.diameter.avp.api.HostIpAddress;
import io.snice.codecs.codec.diameter.avp.api.OriginHost;
import io.snice.codecs.codec.diameter.avp.api.OriginRealm;
import io.snice.codecs.codec.diameter.avp.api.ProductName;
import io.snice.codecs.codec.diameter.avp.api.ResultCode;
import io.snice.codecs.codec.diameter.avp.type.IpAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DiameterTestBase {

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
